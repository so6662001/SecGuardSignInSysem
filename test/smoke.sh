#!/usr/bin/env bash
# 厂智访客 · 端到端冒烟测试（覆盖 开通→登记→审批→在场→离场→台账 与 钢铁 车辆到厂→过磅→放行→回写 及 运营接口）
# 用法：BASE=http://localhost:8080 bash test/smoke.sh
set -uo pipefail
BASE="${BASE:-http://localhost:8080}"
PASS=0; FAIL=0
j() { python3 -c "import sys,json;d=json.load(sys.stdin);print(eval(\"d$1\"))" 2>/dev/null; }
ok() { echo "  ✅ $1"; PASS=$((PASS+1)); }
no() { echo "  ❌ $1"; FAIL=$((FAIL+1)); }
chk() { if [ "$1" = "$2" ]; then ok "$3 ($1)"; else no "$3 期望[$2] 实际[$1]"; fi }

echo "== 健康检查 =="
chk "$(curl -s $BASE/api/public/health | j "['data']['status']")" "UP" "health"

echo "== 运营登录 =="
OPS=$(curl -s -X POST $BASE/api/ops/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}' | j "['data']['accessToken']")
[ -n "$OPS" ] && ok "ops login" || no "ops login"
A="Authorization: Bearer $OPS"; CT="Content-Type: application/json"

echo "== 企业申请开通 =="
M="139$RANDOM$RANDOM"; M="${M:0:11}"
curl -s -X POST $BASE/api/public/apply -H "$CT" -d "{\"companyName\":\"冒烟测试厂\",\"industry\":\"钢铁\",\"contactName\":\"测试\",\"contactMobile\":\"$M\",\"password\":\"smoke888\",\"planCode\":\"STEEL\",\"smsCode\":\"1\"}" >/dev/null
AID=$(curl -s "$BASE/api/ops/applications?status=1" -H "$A" | j "['data']['list'][0]['id']")
[ -n "$AID" ] && ok "apply appId=$AID" || no "apply"
DOM="smoke$RANDOM"
RES=$(curl -s -X POST $BASE/api/ops/applications/$AID/approve -H "$A" -H "$CT" -d "{\"domain\":\"$DOM\"}")
ADMIN=$(echo "$RES" | j "['data']['adminUsername']")
chk "$ADMIN" "$M" "approve→超管账号=手机号"

echo "== 租户登录 =="
TT=$(curl -s -X POST $BASE/api/tenant/auth/login -H "$CT" -d "{\"domain\":\"$DOM\",\"username\":\"$M\",\"password\":\"smoke888\"}" | j "['data']['accessToken']")
[ -n "$TT" ] && ok "tenant login" || no "tenant login"
TA="Authorization: Bearer $TT"

echo "== 门卫入场登记 =="
REC=$(curl -s -X POST $BASE/api/tenant/visits -H "$TA" -H "$CT" -d '{"visitorName":"访客甲","visitorMobile":"13700000000","company":"顺丰","reason":"洽谈业务","hostName":"王工"}')
chk "$(echo "$REC" | j "['data']['status']")" "PENDING" "登记状态"
BADGE=$(echo "$REC" | j "['data']['badgeNo']"); [ -n "$BADGE" ] && ok "访客牌=$BADGE" || no "访客牌"
AP=$(curl -s $BASE/api/tenant/approvals/pending -H "$TA" | j "['data'][0]['id']")
[ -n "$AP" ] && ok "生成待审批 $AP" || no "待审批"

echo "== 审批同意→在场 =="
curl -s -X POST $BASE/api/tenant/approvals/$AP/decision -H "$TA" -H "$CT" -d '{"approve":true}' >/dev/null
chk "$(curl -s $BASE/api/tenant/visits/onsite -H "$TA" | j "['data']['list'][0]['status']")" "ONSITE" "在场状态"
RID=$(curl -s $BASE/api/tenant/visits/onsite -H "$TA" | j "['data']['list'][0]['id']")

echo "== 离场→台账 =="
curl -s -X POST $BASE/api/tenant/visits/$RID/checkout -H "$TA" >/dev/null
chk "$(curl -s "$BASE/api/tenant/visits?status=LEFT" -H "$TA" | j "['data']['list'][0]['status']")" "LEFT" "离场状态"

echo "== 钢铁：车辆到厂→过磅→放行→回写 =="
VV=$(curl -s -X POST $BASE/api/tenant/steel/vehicle-visits -H "$TA" -H "$CT" -d '{"plateNo":"粤B·SMOKE","direction":"OUTBOUND","orderNo":"SO-SMOKE-1","deductRate":0}')
VID=$(echo "$VV" | j "['data']['id']")
[ -n "$VID" ] && ok "车辆到厂 排号$(echo "$VV" | j "['data']['queueNo']")" || no "车辆到厂"
curl -s -X POST $BASE/api/tenant/steel/weigh -H "$TA" -H "$CT" -d "{\"vehicleVisitId\":$VID,\"weight\":12.3,\"weighType\":\"TARE\"}" >/dev/null
W=$(curl -s -X POST $BASE/api/tenant/steel/weigh -H "$TA" -H "$CT" -d "{\"vehicleVisitId\":$VID,\"weight\":42.3,\"weighType\":\"GROSS\"}")
chk "$(echo "$W" | j "['data']['netWeight']")" "30.0" "净重(毛42.3-皮12.3)"
chk "$(curl -s -X POST $BASE/api/tenant/steel/vehicle-visits/$VID/release -H "$TA" | j "['data']['status']")" "RELEASED" "放行状态"

echo "== 运营接口 =="
chk "$(curl -s $BASE/api/ops/funnel -H "$A" | python3 -c "import sys,json;print('REGISTER' in json.load(sys.stdin)['data'])")" "True" "转化漏斗"
Q=$(curl -s -X POST $BASE/api/ops/quotes -H "$A" -H "$CT" -d '{"planCode":"STANDARD","siteCount":2,"addons":["OCR"],"hardware":[]}')
QM=$(echo "$Q" | python3 -c "import sys,json;print(float(json.load(sys.stdin)['data']['monthlyTotal']))" 2>/dev/null)
chk "$QM" "219.0" "报价月费(60*2+99)"

echo ""
echo "== 结果：通过 $PASS · 失败 $FAIL =="
[ "$FAIL" -eq 0 ]
