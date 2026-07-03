-- =====================================================================
-- 厂智访客 · V7 菜单种子 + 申请表登录密码列
-- =====================================================================
SET NAMES utf8mb4;

-- 开通申请：申请人登录密码（BCrypt）
ALTER TABLE tenant_application
  ADD COLUMN password_hash VARCHAR(100) DEFAULT NULL COMMENT '申请人登录密码(BCrypt)' AFTER email;

-- 租户端菜单（app=tenant）
INSERT INTO sys_menu (id, parent_id, name, path, component, icon, perm_code, app, sort, visible) VALUES
 (101, 0, '入场登记',   '/checkin',      'checkin/Index',    'checkin',   'visit:checkin',     'tenant', 1,  1),
 (102, 0, '预约邀请',   '/invite',       'invite/Index',     'invite',    'visit:invite',      'tenant', 2,  1),
 (103, 0, '在场访客',   '/onsite',       'onsite/Index',     'onsite',    'visit:checkout',    'tenant', 3,  1),
 (104, 0, '进出记录',   '/records',      'records/Index',    'records',   'visit:record:view', 'tenant', 4,  1),
 (105, 0, '数据看板',   '/dashboard',    'dashboard/Index',  'chart',     'dashboard:view',    'tenant', 5,  1),
 (106, 0, '审批与通知', '/approval',     'approval/Index',   'bell',      'notify:config',     'tenant', 6,  1),
 (107, 0, '登记项配置', '/field-config', 'field/Index',      'form',      'visit:template',    'tenant', 7,  1),
 (108, 0, '系统设置',   '/settings',     'settings/Index',   'setting',   'member:manage',     'tenant', 8,  1),
 (109, 108, '登记规则', '/rules',        'settings/Rules',   'doc',       'visit:rules',       'tenant', 1,  1),
 (110, 108, '门岗管理', '/gate-config',  'settings/Gate',    'gate',      'gate:manage',       'tenant', 2,  1);

-- 租户端·钢铁专属版菜单（按订阅能力显示，权限点 steel:*）
INSERT INTO sys_menu (id, parent_id, name, path, component, icon, perm_code, app, sort, visible) VALUES
 (121, 0, '车辆到厂',   '/steel/checkin', 'steel/Checkin',   'truck',  'steel:vehicle', 'tenant', 21, 1),
 (122, 0, '磅房过磅',   '/steel/weigh',   'steel/Weigh',     'scale',  'steel:weigh',   'tenant', 22, 1),
 (123, 0, '承运商管理', '/steel/carrier', 'steel/Carrier',   'team',   'steel:carrier', 'tenant', 23, 1);

-- 运营端菜单（app=ops）
INSERT INTO sys_menu (id, parent_id, name, path, component, icon, perm_code, app, sort, visible) VALUES
 (201, 0, '运营概览',   '/platform',     'ops/Overview',     'chart',   NULL,             'ops', 1, 1),
 (202, 0, '开通申请',   '/applications', 'ops/Applications', 'doc',     'ops:application','ops', 2, 1),
 (203, 0, '租户管理',   '/tenants',      'ops/Tenants',      'shop',    'ops:tenant',     'ops', 3, 1),
 (204, 0, '引流转化',   '/conversion',   'ops/Conversion',   'funnel',  'ops:conversion', 'ops', 4, 1),
 (205, 0, '订阅与账单', '/billing',      'ops/Billing',      'wallet',  'ops:billing',    'ops', 5, 1);
