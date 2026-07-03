-- =====================================================================
-- 厂智访客 · V6 初始化种子数据（套餐/增值/硬件/权限/角色/平台超管/内置通知模板）
-- 注意：id 此处用固定值示意，正式可改雪花；密码为 BCrypt('admin123') 占位，务必上线前修改。
-- =====================================================================
SET NAMES utf8mb4;

-- 套餐（对应 pricing.html / quote.html 四档）
INSERT INTO biz_plan (id, code, name, monthly_price, bill_unit, steel_enabled, is_custom, sort) VALUES
 (1, 'FREE',     '免费版',        0.00,  'SITE', 0, 0, 1),
 (2, 'STANDARD', '标准版（通用）', 60.00, 'SITE', 0, 0, 2),
 (3, 'STEEL',    '钢铁厂专属版',   599.00,'SITE', 1, 0, 3),
 (4, 'GROUP',    '集团版（定制）', 0.00,  'SITE', 1, 1, 4);

-- 增值模块（对应 pricing.html 增值模块）
INSERT INTO biz_addon (id, code, name, bill_type, price, unit, scope) VALUES
 (1, 'LPR',     '车牌识别 + 道闸联动',      'MONTHLY', 199.00, NULL,   'ALL'),
 (2, 'FACE',    '人脸识别核验',            'MONTHLY', 159.00, NULL,   'ALL'),
 (3, 'OCR',     '身份证 OCR 识别',         'MONTHLY', 99.00,  NULL,   'ALL'),
 (4, 'ESIGN',   '电子承诺书 / 保密协议',    'USAGE',   0.50,   '份',   'ALL'),
 (5, 'SMS',     '短信 / 语音通知包',        'USAGE',   30.00,  '千条', 'ALL'),
 (6, 'TRADE',   '磅单对接 + 交易平台打通',  'MONTHLY', 399.00, NULL,   'STEEL');

-- 硬件（对应 pricing.html / quote.html）
INSERT INTO biz_hardware (id, code, name, buy_price, rent_price, type) VALUES
 (1, 'TABLET',   '门岗登记平板',   1280.00, 49.00,  'TABLET'),
 (2, 'FACE_ALL', '人脸识别一体机', 2680.00, 99.00,  'FACE'),
 (3, 'LPR_GATE', '车牌识别道闸',   5600.00, 199.00, 'LPR_GATE'),
 (4, 'QR_STAND', '自助登记码立牌', 120.00,  NULL,   'QR_STAND');

-- 权限点（示例，实际按模块补全到按钮级）
INSERT INTO sys_permission (id, code, name, module, type, scope) VALUES
 (1,  'visit:checkin',      '入场登记',      'visitor', 2, 2),
 (2,  'visit:checkout',     '离场登记',      'visitor', 2, 2),
 (3,  'visit:record:view',  '查看进出记录',  'visitor', 2, 2),
 (4,  'visit:record:audit', '审核进出记录',  'visitor', 2, 2),
 (5,  'visit:template',     '登记项配置',    'visitor', 2, 2),
 (6,  'visit:rules',        '登记规则',      'visitor', 2, 2),
 (7,  'visit:invite',       '预约邀请',      'visitor', 2, 2),
 (8,  'gate:manage',        '门岗管理',      'system',  2, 2),
 (9,  'member:manage',      '人员与权限',    'system',  2, 2),
 (10, 'dashboard:view',     '数据看板',      'visitor', 2, 2),
 (11, 'notify:config',      '审批与通知',    'notify',  2, 2),
 (12, 'steel:vehicle',      '车辆到厂',      'steel',   2, 2),
 (13, 'steel:weigh',        '磅房过磅',      'steel',   2, 2),
 (14, 'steel:carrier',      '承运商管理',    'steel',   2, 2),
 (20, 'ops:application',    '开通申请审核',  'ops',     2, 1),
 (21, 'ops:tenant',         '租户管理',      'ops',     2, 1),
 (22, 'ops:conversion',     '引流转化',      'ops',     2, 1),
 (23, 'ops:billing',        '订阅与账单',    'ops',     2, 1),
 (24, 'ops:quote',          '报价单',        'ops',     2, 1);

-- 平台内置角色
INSERT INTO sys_role (id, tenant_id, name, code, data_scope, builtin) VALUES
 (1, 0, '平台超级管理员', 'PLATFORM_ADMIN', 1, 1),
 (2, 0, '运营',           'PLATFORM_OPS',   1, 1);

-- 平台超管账号（tenant_id=0）；密码为 BCrypt 占位，请上线前重置
INSERT INTO sys_user (id, tenant_id, username, password, real_name, user_type, status) VALUES
 (1, 0, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '平台管理员', 1, 1);
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 租户内置角色模板（开通租户时按此复制生成，tenant_id 替换为新租户）
-- 企业超管/管理员/审核员/门卫/司磅员/被访人 —— 由 fv-ops 开通流程动态创建，这里仅登记权限点便于程序引用。

-- 内置通知模板（平台默认，tenant_id=0）
INSERT INTO notify_template (id, tenant_id, code, channel, title, content, scene) VALUES
 (1, 0, 'VISIT_APPROVE_REQ', 'SMS',  '到访审批', '【{company}】访客{visitor}（{unit}）因{reason}拜访您，点击审批：{link}（10分钟内有效）', 'visit_approve'),
 (2, 0, 'VISIT_APPROVE_REQ', 'PUSH', '新的到访请求', '{visitor} 申请到访 · {unit} · {reason}', 'visit_approve'),
 (3, 0, 'VISIT_APPROVED',    'PUSH', '访客已放行', '您的访客 {visitor} 已入场', 'visit_result'),
 (4, 0, 'OVERSTAY_REMIND',   'PUSH', '滞留提醒', '访客 {visitor} 在场已超 {hours} 小时，请核实', 'overstay'),
 (5, 0, 'TRIAL_EXPIRE',      'SMS',  '试用到期提醒', '【厂智访客】您的试用将于 {date} 到期，续费不中断服务', 'billing'),
 (6, 0, 'WEIGH_ABNORMAL',    'PUSH', '磅单异常', '车辆 {plate} 磅单出现 {abnormal}，请核对', 'steel'),
 (7, 0, 'DEVICE_OFFLINE',    'PUSH', '设备离线', '设备 {device} 已离线，请检查', 'device');
