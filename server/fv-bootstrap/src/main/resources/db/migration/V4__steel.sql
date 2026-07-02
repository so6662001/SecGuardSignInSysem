-- =====================================================================
-- 厂智访客 · V4 钢铁厂专属域（承运商/车辆/司机/车辆到厂/磅单/交易平台单据映射）
-- 租户业务表，含 tenant_id；仅订阅含钢铁模块的租户使用。
-- =====================================================================
SET NAMES utf8mb4;

-- 承运商
CREATE TABLE carrier (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  name          VARCHAR(120) NOT NULL,
  cooperate_since VARCHAR(30)         DEFAULT NULL,
  allowed_goods VARCHAR(120)          DEFAULT NULL COMMENT '准运货物',
  trade_joined  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否入驻交易平台运力池',
  whitelist     TINYINT      NOT NULL DEFAULT 0 COMMENT '白名单免审',
  status        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/RESTRICT/BLACK',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_carrier_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='承运商';

-- 车辆
CREATE TABLE vehicle (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  plate_no      VARCHAR(20)  NOT NULL COMMENT '车牌',
  carrier_id    BIGINT                DEFAULT NULL,
  vehicle_type  VARCHAR(30)           DEFAULT NULL,
  allowed_goods VARCHAR(120)          DEFAULT NULL,
  whitelist     TINYINT      NOT NULL DEFAULT 0,
  status        VARCHAR(20)  NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/RESTRICT/BLACK',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_vehicle_plate (tenant_id, plate_no),
  KEY idx_vehicle_carrier (carrier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆';

-- 司机
CREATE TABLE driver (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  name          VARCHAR(50)  NOT NULL,
  mobile        VARCHAR(64)           DEFAULT NULL,
  carrier_id    BIGINT                DEFAULT NULL,
  vehicle_id    BIGINT                DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_driver_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机';

-- 车辆到厂
CREATE TABLE vehicle_visit (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  site_id       BIGINT                DEFAULT NULL,
  gate_id       BIGINT                DEFAULT NULL,
  plate_no      VARCHAR(20)  NOT NULL,
  carrier_id    BIGINT                DEFAULT NULL,
  driver_id     BIGINT                DEFAULT NULL,
  direction     VARCHAR(20)  NOT NULL COMMENT 'INBOUND送货入厂/OUTBOUND提货出厂',
  order_no      VARCHAR(50)           DEFAULT NULL COMMENT '关联单据号',
  goods_name    VARCHAR(80)           DEFAULT NULL,
  goods_spec    VARCHAR(80)           DEFAULT NULL,
  plan_weight   DECIMAL(12,3)         DEFAULT NULL COMMENT '计划重量(吨)',
  warehouse     VARCHAR(80)           DEFAULT NULL COMMENT '仓库/料场/垛位',
  deduct_rate   DECIMAL(6,3)          DEFAULT NULL COMMENT '扣杂率(%)',
  safety_check  JSON                  DEFAULT NULL COMMENT '安全合规项',
  queue_no      VARCHAR(20)           DEFAULT NULL COMMENT '排队号',
  status        VARCHAR(20)  NOT NULL DEFAULT 'REGISTERED' COMMENT 'BOOKED/ARRIVED/REGISTERED/WEIGHING/LOADING/REWEIGH/RELEASED',
  timeline      JSON                  DEFAULT NULL COMMENT '闭环时间线',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_vv_tenant_status (tenant_id, status),
  KEY idx_vv_plate (plate_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆到厂';

-- 磅单
CREATE TABLE weigh_record (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  vehicle_visit_id BIGINT    NOT NULL,
  device_id     BIGINT                DEFAULT NULL COMMENT '地磅设备',
  tare_weight   DECIMAL(12,3)         DEFAULT NULL COMMENT '皮重',
  gross_weight  DECIMAL(12,3)         DEFAULT NULL COMMENT '毛重',
  net_weight    DECIMAL(12,3)         DEFAULT NULL COMMENT '净重',
  deduct_rate   DECIMAL(6,3)          DEFAULT NULL,
  settle_weight DECIMAL(12,3)         DEFAULT NULL COMMENT '结算净重',
  tare_time     DATETIME              DEFAULT NULL,
  gross_time    DATETIME              DEFAULT NULL,
  snapshot_urls JSON                  DEFAULT NULL COMMENT '抓拍存证',
  abnormal      VARCHAR(30)           DEFAULT NULL COMMENT 'OVERLOAD/REWEIGH_DIFF',
  written_back  TINYINT      NOT NULL DEFAULT 0 COMMENT '是否回写交易平台',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_wr_visit (vehicle_visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='磅单';

-- 交易平台单据映射
CREATE TABLE trade_order_ref (
  id            BIGINT       NOT NULL,
  tenant_id     BIGINT       NOT NULL,
  vehicle_visit_id BIGINT             DEFAULT NULL,
  ext_order_no  VARCHAR(50)  NOT NULL COMMENT '外部订单/采购单号',
  order_type    VARCHAR(20)  NOT NULL COMMENT 'SALES销售/PURCHASE采购',
  partner_name  VARCHAR(120)          DEFAULT NULL COMMENT '客户/供应商',
  goods_name    VARCHAR(80)           DEFAULT NULL,
  plan_qty      DECIMAL(12,3)         DEFAULT NULL COMMENT '应提/应收量',
  done_qty      DECIMAL(12,3)         DEFAULT NULL COMMENT '已提/已收',
  remain_qty    DECIMAL(12,3)         DEFAULT NULL COMMENT '剩余',
  settle_status VARCHAR(20)           DEFAULT NULL COMMENT '结算状态',
  sync_time     DATETIME              DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tor_order (ext_order_no),
  KEY idx_tor_visit (vehicle_visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易平台单据映射';
