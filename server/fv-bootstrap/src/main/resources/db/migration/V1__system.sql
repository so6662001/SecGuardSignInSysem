-- =====================================================================
-- 厂智访客 · V1 系统域（用户/角色/权限/菜单/厂区/门岗/设备/审计）
-- MySQL 8 / InnoDB / utf8mb4。租户业务表含 tenant_id（平台账号 tenant_id=0）。
-- 公共字段：create_time/update_time/create_by/update_by/deleted
-- =====================================================================
SET NAMES utf8mb4;

-- 厂区（多厂区/集团）
CREATE TABLE sys_site (
  id           BIGINT       NOT NULL COMMENT '主键(雪花)',
  tenant_id    BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID',
  name         VARCHAR(100) NOT NULL COMMENT '厂区名称',
  address      VARCHAR(255)          DEFAULT NULL COMMENT '地址',
  manager      VARCHAR(50)           DEFAULT NULL COMMENT '负责人',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:0停用 1启用',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by    BIGINT                DEFAULT NULL,
  update_by    BIGINT                DEFAULT NULL,
  deleted      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_site_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='厂区';

-- 用户（平台与租户用户；平台账号 tenant_id=0）
CREATE TABLE sys_user (
  id           BIGINT       NOT NULL COMMENT '主键(雪花)',
  tenant_id    BIGINT       NOT NULL DEFAULT 0 COMMENT '租户ID(0=平台)',
  username     VARCHAR(64)  NOT NULL COMMENT '登录账号',
  password     VARCHAR(100) NOT NULL COMMENT 'BCrypt密码',
  real_name    VARCHAR(50)           DEFAULT NULL COMMENT '姓名',
  mobile       VARCHAR(20)           DEFAULT NULL COMMENT '手机号(加密)',
  mobile_mask  VARCHAR(20)           DEFAULT NULL COMMENT '手机号脱敏展示',
  email        VARCHAR(100)          DEFAULT NULL COMMENT '邮箱',
  avatar       VARCHAR(255)          DEFAULT NULL COMMENT '头像',
  job_title    VARCHAR(50)           DEFAULT NULL COMMENT '职务',
  site_id      BIGINT                DEFAULT NULL COMMENT '所属厂区',
  gate_id      BIGINT                DEFAULT NULL COMMENT '值守门岗(门卫)',
  user_type    TINYINT      NOT NULL DEFAULT 2 COMMENT '类型:1平台 2租户',
  wx_openid    VARCHAR(64)           DEFAULT NULL COMMENT '微信openid',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  last_login   DATETIME              DEFAULT NULL COMMENT '最近登录',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by    BIGINT                DEFAULT NULL,
  update_by    BIGINT                DEFAULT NULL,
  deleted      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_tenant_name (tenant_id, username, deleted),
  KEY idx_user_tenant (tenant_id),
  KEY idx_user_mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 角色
CREATE TABLE sys_role (
  id           BIGINT       NOT NULL,
  tenant_id    BIGINT       NOT NULL DEFAULT 0,
  name         VARCHAR(50)  NOT NULL COMMENT '角色名',
  code         VARCHAR(50)  NOT NULL COMMENT '角色编码',
  data_scope   TINYINT      NOT NULL DEFAULT 1 COMMENT '数据范围:1全部 2本厂区 3本门岗 4本人',
  builtin      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否内置',
  remark       VARCHAR(255)          DEFAULT NULL,
  status       TINYINT      NOT NULL DEFAULT 1,
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by    BIGINT                DEFAULT NULL,
  update_by    BIGINT                DEFAULT NULL,
  deleted      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_role_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 权限点
CREATE TABLE sys_permission (
  id           BIGINT       NOT NULL,
  code         VARCHAR(100) NOT NULL COMMENT '权限编码 如 visit:record:audit',
  name         VARCHAR(100) NOT NULL COMMENT '权限名',
  module       VARCHAR(50)           DEFAULT NULL COMMENT '所属模块',
  type         TINYINT      NOT NULL DEFAULT 2 COMMENT '1菜单 2按钮/操作',
  scope        TINYINT      NOT NULL DEFAULT 2 COMMENT '1平台 2租户 3通用',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perm_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点';

-- 角色-权限
CREATE TABLE sys_role_permission (
  id            BIGINT NOT NULL,
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rp (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- 用户-角色
CREATE TABLE sys_user_role (
  id       BIGINT NOT NULL,
  user_id  BIGINT NOT NULL,
  role_id  BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ur (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 菜单（前端动态路由）
CREATE TABLE sys_menu (
  id            BIGINT       NOT NULL,
  parent_id     BIGINT       NOT NULL DEFAULT 0,
  name          VARCHAR(50)  NOT NULL COMMENT '菜单名',
  path          VARCHAR(200)          DEFAULT NULL COMMENT '路由',
  component     VARCHAR(200)          DEFAULT NULL COMMENT '组件',
  icon          VARCHAR(50)           DEFAULT NULL,
  perm_code     VARCHAR(100)          DEFAULT NULL COMMENT '关联权限点',
  app           VARCHAR(20)  NOT NULL DEFAULT 'tenant' COMMENT '归属端:tenant/ops',
  sort          INT          NOT NULL DEFAULT 0,
  visible       TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_menu_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单';

-- 门岗
CREATE TABLE sys_gate (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL DEFAULT 0,
  site_id       BIGINT                DEFAULT NULL COMMENT '所属厂区',
  name          VARCHAR(100) NOT NULL COMMENT '门岗名称',
  gate_code     VARCHAR(64)  NOT NULL COMMENT '门岗码(自助登记二维码标识)',
  type          TINYINT      NOT NULL DEFAULT 1 COMMENT '1人行 2车行 3人车混合',
  location      VARCHAR(255)          DEFAULT NULL,
  guard_ids     VARCHAR(255)          DEFAULT NULL COMMENT '值守门卫ID(逗号分隔)',
  qrcode_url    VARCHAR(255)          DEFAULT NULL COMMENT '自助登记二维码URL',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by     BIGINT                DEFAULT NULL,
  update_by     BIGINT                DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_gate_code (gate_code),
  KEY idx_gate_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门岗';

-- 设备
CREATE TABLE sys_device (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL DEFAULT 0,
  gate_id       BIGINT                DEFAULT NULL COMMENT '绑定门岗',
  site_id       BIGINT                DEFAULT NULL,
  name          VARCHAR(100) NOT NULL COMMENT '设备名称',
  device_no     VARCHAR(64)  NOT NULL COMMENT '设备编号',
  type          VARCHAR(30)  NOT NULL COMMENT 'TABLET/LPR_GATE/CAMERA/FACE/QR_STAND/WEIGHBRIDGE',
  vendor        VARCHAR(50)           DEFAULT NULL COMMENT '厂商:hik/dahua/mock...',
  model         VARCHAR(80)           DEFAULT NULL,
  conn_type     VARCHAR(30)           DEFAULT NULL COMMENT 'HTTP/ISAPI/TCP/SERIAL/MQTT',
  secret        VARCHAR(128)          DEFAULT NULL COMMENT '设备密钥(鉴权)',
  online        TINYINT      NOT NULL DEFAULT 0 COMMENT '0离线 1在线',
  last_heartbeat DATETIME             DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by     BIGINT                DEFAULT NULL,
  update_by     BIGINT                DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_device_no (device_no),
  KEY idx_device_tenant (tenant_id),
  KEY idx_device_gate (gate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备';

-- 审计日志
CREATE TABLE sys_audit_log (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL DEFAULT 0,
  user_id       BIGINT                DEFAULT NULL,
  user_name     VARCHAR(50)           DEFAULT NULL,
  module        VARCHAR(50)           DEFAULT NULL,
  action        VARCHAR(50)           DEFAULT NULL COMMENT '动作',
  target        VARCHAR(100)          DEFAULT NULL COMMENT '对象',
  detail        JSON                  DEFAULT NULL COMMENT '前后值/详情',
  ip            VARCHAR(50)           DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_tenant_time (tenant_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审计日志';
