-- =====================================================================
-- 厂智访客 · V5 通知/提醒域（模板/任务/日志/租户渠道配置）
-- =====================================================================
SET NAMES utf8mb4;

-- 通知模板
CREATE TABLE notify_template (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL DEFAULT 0 COMMENT '0=平台内置默认',
  code          VARCHAR(60)  NOT NULL COMMENT '模板编码/场景',
  channel       VARCHAR(20)  NOT NULL COMMENT 'PUSH/SMS/VOICE/WECHAT/WECOM/DINGTALK',
  title         VARCHAR(120)          DEFAULT NULL,
  content       VARCHAR(1000)         DEFAULT NULL COMMENT '含占位变量',
  scene         VARCHAR(60)           DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tpl_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知模板';

-- 通知任务
CREATE TABLE notify_task (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL DEFAULT 0,
  scene         VARCHAR(60)  NOT NULL COMMENT '业务场景',
  biz_id        BIGINT                DEFAULT NULL COMMENT '关联业务ID(如审批ID)',
  target_user   BIGINT                DEFAULT NULL,
  target_mobile VARCHAR(64)           DEFAULT NULL,
  channels      JSON                  DEFAULT NULL COMMENT '渠道链(优先级顺序)',
  timeout_sec   INT                   DEFAULT NULL COMMENT '响应时限',
  escalation    JSON                  DEFAULT NULL COMMENT '升级策略',
  status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SENT/RESPONDED/ESCALATED/DONE/CANCELLED',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_task_biz (scene, biz_id),
  KEY idx_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知任务';

-- 通知发送日志
CREATE TABLE notify_log (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL DEFAULT 0,
  task_id       BIGINT                DEFAULT NULL,
  channel       VARCHAR(20)  NOT NULL,
  target        VARCHAR(64)           DEFAULT NULL,
  content       VARCHAR(1000)         DEFAULT NULL,
  result        VARCHAR(20)  NOT NULL DEFAULT 'SENT' COMMENT 'SENT/SUCCESS/FAIL',
  receipt       VARCHAR(255)          DEFAULT NULL COMMENT '回执',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_log_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知发送日志';

-- 租户渠道配置（对应 approval.html）
CREATE TABLE notify_channel_config (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  channel_order JSON                  DEFAULT NULL COMMENT '渠道优先级与开关',
  first_timeout INT          NOT NULL DEFAULT 180 COMMENT '首次响应时限(秒)',
  second_remind TINYINT      NOT NULL DEFAULT 1 COMMENT '二次提醒换渠道',
  escalate_backup TINYINT    NOT NULL DEFAULT 1 COMMENT '升级备用审批人',
  guard_proxy   TINYINT      NOT NULL DEFAULT 1 COMMENT '门卫代批兜底',
  invite_pass   TINYINT      NOT NULL DEFAULT 1 COMMENT '预约邀请码免审',
  whitelist_pass TINYINT     NOT NULL DEFAULT 1 COMMENT '白名单免审',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ncc_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户通知渠道配置';
