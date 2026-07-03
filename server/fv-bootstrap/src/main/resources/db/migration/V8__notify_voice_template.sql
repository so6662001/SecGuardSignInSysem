-- =====================================================================
-- 厂智访客 · V8 补充语音审批通知模板（超时升级二次提醒用）
-- =====================================================================
SET NAMES utf8mb4;

INSERT INTO notify_template (id, tenant_id, code, channel, title, content, scene) VALUES
 (8, 0, 'VISIT_APPROVE_REQ', 'VOICE', '到访审批语音', '您有访客{visitor}（{unit}）因{reason}到访，同意请按1，拒绝请按2', 'visit_approve');
