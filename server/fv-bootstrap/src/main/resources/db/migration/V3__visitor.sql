-- =====================================================================
-- 厂智访客 · V3 访客通用域（模板/字段/访客/记录/审批/邀请/黑名单/访客牌/规则）
-- 均为租户业务表，含 tenant_id。
-- =====================================================================
SET NAMES utf8mb4;

-- 登记模板
CREATE TABLE register_template (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  name          VARCHAR(80)  NOT NULL COMMENT '模板名',
  visit_type    VARCHAR(30)  NOT NULL COMMENT 'NORMAL/CONTRACTOR/SUPPLIER/INTERVIEW/STEEL_VEHICLE',
  enabled_modules JSON                DEFAULT NULL COMMENT '启用模块列表',
  is_default    TINYINT      NOT NULL DEFAULT 0,
  sort          INT          NOT NULL DEFAULT 0,
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by     BIGINT                DEFAULT NULL,
  update_by     BIGINT                DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_tpl_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登记模板';

-- 登记字段
CREATE TABLE register_field (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  template_id   BIGINT       NOT NULL,
  module        VARCHAR(30)  NOT NULL COMMENT 'BASIC/ID/DEVICE/DANGER/PLEDGE/HEALTH/VEHICLE/CUSTOM',
  field_key     VARCHAR(50)  NOT NULL COMMENT '字段标识',
  label         VARCHAR(80)  NOT NULL,
  field_type    VARCHAR(20)  NOT NULL COMMENT 'TEXT/TEXTAREA/RADIO/CHECKBOX/NUMBER/DATE/PHOTO/SIGN/SWITCH',
  required      TINYINT      NOT NULL DEFAULT 0,
  options       JSON                  DEFAULT NULL COMMENT '选项',
  sort          INT          NOT NULL DEFAULT 0,
  status        TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_field_tpl (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登记字段';

-- 访客主档
CREATE TABLE visitor (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  name          VARCHAR(50)  NOT NULL,
  mobile        VARCHAR(64)           DEFAULT NULL COMMENT '手机(加密)',
  mobile_mask   VARCHAR(20)           DEFAULT NULL,
  company       VARCHAR(100)          DEFAULT NULL,
  id_type       VARCHAR(20)           DEFAULT NULL,
  id_no         VARCHAR(128)          DEFAULT NULL COMMENT '证件号(加密)',
  face_url      VARCHAR(255)          DEFAULT NULL,
  is_frequent   TINYINT      NOT NULL DEFAULT 0 COMMENT '常客',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_visitor_tenant_mobile (tenant_id, mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客主档';

-- 进出记录
CREATE TABLE visit_record (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  site_id       BIGINT                DEFAULT NULL,
  gate_id       BIGINT                DEFAULT NULL,
  template_id   BIGINT                DEFAULT NULL,
  visitor_id    BIGINT                DEFAULT NULL,
  visitor_name  VARCHAR(50)  NOT NULL,
  visitor_mobile VARCHAR(64)          DEFAULT NULL,
  company       VARCHAR(100)          DEFAULT NULL,
  reason        VARCHAR(50)           DEFAULT NULL COMMENT '来访事由',
  host_name     VARCHAR(50)           DEFAULT NULL COMMENT '被访人',
  host_user_id  BIGINT                DEFAULT NULL,
  host_dept     VARCHAR(80)           DEFAULT NULL,
  companions    INT          NOT NULL DEFAULT 1 COMMENT '随行人数',
  plate_no      VARCHAR(20)           DEFAULT NULL COMMENT '车牌',
  badge_no      VARCHAR(30)           DEFAULT NULL COMMENT '访客牌',
  register_type VARCHAR(20)  NOT NULL DEFAULT 'GUARD' COMMENT 'GUARD/SELF/ID_OCR',
  register_by   BIGINT                DEFAULT NULL COMMENT '登记人',
  in_time       DATETIME              DEFAULT NULL COMMENT '入场时间',
  out_time      DATETIME              DEFAULT NULL COMMENT '离场时间',
  status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ONSITE/LEFT/REJECTED/OVERSTAY',
  ext_fields    JSON                  DEFAULT NULL COMMENT '扩展字段值',
  devices       JSON                  DEFAULT NULL COMMENT '携带设备明细',
  pledge_url    VARCHAR(255)          DEFAULT NULL COMMENT '承诺书签署存证',
  invitation_id BIGINT                DEFAULT NULL COMMENT '来源邀请',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_vr_tenant_status (tenant_id, status),
  KEY idx_vr_in_time (in_time),
  KEY idx_vr_host (host_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='进出记录';

-- 审批
CREATE TABLE visit_approval (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  record_id     BIGINT       NOT NULL,
  host_user_id  BIGINT                DEFAULT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'WAITING' COMMENT 'WAITING/APPROVED/REJECTED/ESCALATED/GUARD_PROXY',
  approver_id   BIGINT                DEFAULT NULL,
  channel       VARCHAR(20)           DEFAULT NULL COMMENT '响应渠道',
  valid_scope   VARCHAR(20)           DEFAULT NULL COMMENT 'ONCE/TODAY/WEEK',
  cost_seconds  INT                   DEFAULT NULL COMMENT '耗时',
  trace         JSON                  DEFAULT NULL COMMENT '流转轨迹',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_appr_record (record_id),
  KEY idx_appr_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客审批';

-- 预约邀请
CREATE TABLE invitation (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  visitor_name  VARCHAR(50)  NOT NULL,
  visitor_mobile VARCHAR(64)          DEFAULT NULL,
  company       VARCHAR(100)          DEFAULT NULL,
  reason        VARCHAR(50)           DEFAULT NULL,
  host_user_id  BIGINT                DEFAULT NULL,
  host_name     VARCHAR(50)           DEFAULT NULL,
  companions    INT          NOT NULL DEFAULT 1,
  visit_date    DATE                  DEFAULT NULL,
  time_from     VARCHAR(10)           DEFAULT NULL,
  time_to       VARCHAR(10)           DEFAULT NULL,
  invite_code   VARCHAR(20)  NOT NULL COMMENT '邀请码',
  qrcode_url    VARCHAR(255)          DEFAULT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ARRIVED/EXPIRED/CANCELLED',
  create_by     BIGINT                DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invite_code (invite_code),
  KEY idx_invite_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约邀请';

-- 黑名单/限制
CREATE TABLE blacklist (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  target_type   VARCHAR(20)  NOT NULL COMMENT 'MOBILE/ID/COMPANY',
  target_value  VARCHAR(128) NOT NULL,
  list_type     VARCHAR(20)  NOT NULL DEFAULT 'BLACK' COMMENT 'BLACK/RESTRICT',
  reason        VARCHAR(255)          DEFAULT NULL,
  create_by     BIGINT                DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_bl_tenant_val (tenant_id, target_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单与限制';

-- 访客牌
CREATE TABLE visitor_badge (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  badge_no      VARCHAR(30)  NOT NULL,
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0可用 1占用',
  record_id     BIGINT                DEFAULT NULL COMMENT '当前占用记录',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_badge (tenant_id, badge_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客牌';

-- 登记规则（每租户一条，JSON 存各项开关/阈值）
CREATE TABLE tenant_rules (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  info_rules    JSON                  DEFAULT NULL COMMENT '信息必填项',
  approval_rules JSON                 DEFAULT NULL COMMENT '审批放行',
  stay_rules    JSON                  DEFAULT NULL COMMENT '在场与时长(滞留/自动离场/通行时段)',
  badge_rules   JSON                  DEFAULT NULL COMMENT '访客牌(分配/归还/号段)',
  privacy_rules JSON                  DEFAULT NULL COMMENT '数据留存/脱敏/授权',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_rules_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登记规则';
