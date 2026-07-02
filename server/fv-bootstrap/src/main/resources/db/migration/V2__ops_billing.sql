-- =====================================================================
-- 厂智访客 · V2 运营与计费域（租户/申请/套餐/增值/硬件/订阅/账单/报价/引流）
-- 平台域表：不参与租户过滤（TenantLineInnerInterceptor 忽略清单）。
-- =====================================================================
SET NAMES utf8mb4;

-- 租户企业
CREATE TABLE sys_tenant (
  id            BIGINT       NOT NULL COMMENT '租户ID',
  name          VARCHAR(150) NOT NULL COMMENT '企业全称',
  credit_code   VARCHAR(30)           DEFAULT NULL COMMENT '统一社会信用代码',
  industry      VARCHAR(50)           DEFAULT NULL COMMENT '行业',
  address       VARCHAR(255)          DEFAULT NULL,
  domain        VARCHAR(80)           DEFAULT NULL COMMENT '专属登录域名',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1试用 2正常 3即将到期 4欠费 5停用',
  source        VARCHAR(50)           DEFAULT NULL COMMENT '来源',
  open_date     DATE                  DEFAULT NULL COMMENT '开通日期',
  site_count    INT          NOT NULL DEFAULT 1 COMMENT '厂区数',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  create_by     BIGINT                DEFAULT NULL,
  update_by     BIGINT                DEFAULT NULL,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tenant_domain (domain),
  KEY idx_tenant_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户企业';

-- 开通申请
CREATE TABLE tenant_application (
  id            BIGINT       NOT NULL,
  app_no        VARCHAR(40)  NOT NULL COMMENT '申请编号',
  company_name  VARCHAR(150) NOT NULL,
  credit_code   VARCHAR(30)           DEFAULT NULL,
  industry      VARCHAR(50)           DEFAULT NULL,
  address       VARCHAR(255)          DEFAULT NULL,
  site_count    INT          NOT NULL DEFAULT 1,
  contact_name  VARCHAR(50)           DEFAULT NULL,
  contact_mobile VARCHAR(20)          DEFAULT NULL,
  contact_title VARCHAR(50)           DEFAULT NULL,
  email         VARCHAR(100)          DEFAULT NULL,
  plan_code     VARCHAR(50)           DEFAULT NULL COMMENT '申请套餐',
  license_files JSON                  DEFAULT NULL COMMENT '证照文件URL',
  gs_verify     JSON                  DEFAULT NULL COMMENT '工商核验结果',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1待审 2通过开通 3驳回 4补充材料',
  audit_by      BIGINT                DEFAULT NULL,
  audit_remark  VARCHAR(500)          DEFAULT NULL,
  domain        VARCHAR(80)           DEFAULT NULL COMMENT '开通分配域名',
  tenant_id     BIGINT                DEFAULT NULL COMMENT '开通后租户ID',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_app_no (app_no),
  KEY idx_app_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开通申请';

-- 套餐
CREATE TABLE biz_plan (
  id            BIGINT       NOT NULL,
  code          VARCHAR(50)  NOT NULL,
  name          VARCHAR(80)  NOT NULL,
  monthly_price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '月单价',
  bill_unit     VARCHAR(20)  NOT NULL DEFAULT 'SITE' COMMENT '计费单位:厂区',
  features      JSON                  DEFAULT NULL COMMENT '功能项',
  steel_enabled TINYINT      NOT NULL DEFAULT 0 COMMENT '是否含钢铁模块',
  is_custom     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否面议(集团版)',
  sort          INT          NOT NULL DEFAULT 0,
  status        TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_plan_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐';

-- 增值模块
CREATE TABLE biz_addon (
  id            BIGINT       NOT NULL,
  code          VARCHAR(50)  NOT NULL,
  name          VARCHAR(80)  NOT NULL,
  bill_type     VARCHAR(20)  NOT NULL DEFAULT 'MONTHLY' COMMENT 'MONTHLY/USAGE',
  price         DECIMAL(10,2) NOT NULL DEFAULT 0,
  unit          VARCHAR(20)           DEFAULT NULL COMMENT '计价单位 如 千条/份',
  scope         VARCHAR(20)  NOT NULL DEFAULT 'ALL' COMMENT 'ALL/STEEL',
  status        TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_addon_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='增值模块';

-- 硬件
CREATE TABLE biz_hardware (
  id            BIGINT       NOT NULL,
  code          VARCHAR(50)  NOT NULL,
  name          VARCHAR(80)  NOT NULL,
  buy_price     DECIMAL(10,2)         DEFAULT NULL COMMENT '购买价',
  rent_price    DECIMAL(10,2)         DEFAULT NULL COMMENT '租赁月价',
  type          VARCHAR(30)           DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 1,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_hw_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='硬件';

-- 订阅
CREATE TABLE tenant_subscription (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  plan_code     VARCHAR(50)  NOT NULL,
  site_count    INT          NOT NULL DEFAULT 1,
  addons        JSON                  DEFAULT NULL COMMENT '已开通增值模块',
  monthly_fee   DECIMAL(10,2) NOT NULL DEFAULT 0,
  period        VARCHAR(20)  NOT NULL DEFAULT 'MONTH',
  trial_end     DATE                  DEFAULT NULL COMMENT '试用到期',
  next_renew    DATE                  DEFAULT NULL COMMENT '下次续费',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1试用 2正常 3欠费 4停用',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_sub_tenant (tenant_id),
  KEY idx_sub_renew (next_renew)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户订阅';

-- 账单
CREATE TABLE biz_invoice (
  id            BIGINT       NOT NULL,
  invoice_no    VARCHAR(40)  NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  period        VARCHAR(20)           DEFAULT NULL COMMENT '账期 如 2026-07',
  amount        DECIMAL(10,2) NOT NULL DEFAULT 0,
  pay_method    VARCHAR(30)           DEFAULT NULL,
  status        TINYINT      NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2试用',
  pay_time      DATETIME              DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invoice_no (invoice_no),
  KEY idx_invoice_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单';

-- 报价单
CREATE TABLE biz_quote (
  id            BIGINT       NOT NULL,
  quote_no      VARCHAR(40)  NOT NULL,
  customer_name VARCHAR(150)          DEFAULT NULL,
  contact_name  VARCHAR(50)           DEFAULT NULL,
  contact_phone VARCHAR(20)           DEFAULT NULL,
  sales_name    VARCHAR(50)           DEFAULT NULL,
  items         JSON                  DEFAULT NULL COMMENT '明细',
  monthly_total DECIMAL(12,2)         DEFAULT NULL,
  onetime_total DECIMAL(12,2)         DEFAULT NULL,
  first_year    DECIMAL(12,2)         DEFAULT NULL,
  is_custom     TINYINT      NOT NULL DEFAULT 0 COMMENT '含面议',
  valid_days    INT          NOT NULL DEFAULT 30,
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1草稿 2已发送 3已成交 4已失效',
  create_by     BIGINT                DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_quote_no (quote_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报价单';

-- 引流线索
CREATE TABLE ops_lead (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT                DEFAULT NULL COMMENT '来源租户',
  company_name  VARCHAR(150)          DEFAULT NULL,
  signal_tag    VARCHAR(50)           DEFAULT NULL COMMENT '行业信号:高频物流/供应链活跃/运力密集',
  recommend     VARCHAR(80)           DEFAULT NULL COMMENT '推荐产品:交易平台/管理软件/MES',
  intent_score  INT          NOT NULL DEFAULT 0 COMMENT '意向分',
  status        TINYINT      NOT NULL DEFAULT 1 COMMENT '1待跟进 2跟进中 3成交 4观察',
  owner_id      BIGINT                DEFAULT NULL COMMENT '负责人',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_lead_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='引流线索';

-- 转化事件流水
CREATE TABLE ops_conversion (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT                DEFAULT NULL,
  stage         VARCHAR(30)  NOT NULL COMMENT 'REGISTER/ACTIVE/POTENTIAL/OPPORTUNITY/DEAL',
  product       VARCHAR(50)           DEFAULT NULL COMMENT '成交去向',
  amount        DECIMAL(12,2)         DEFAULT NULL COMMENT '成交金额',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_conv_stage (stage),
  KEY idx_conv_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='引流转化事件';
