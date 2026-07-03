package cn.faccess.device.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * 设备与第三方能力适配器（接口 + Mock 实现）。
 * 生产用 @ConditionalOnProperty 切换海康/大华/阿里/腾讯/e签宝等真实实现。
 */
public class DeviceAdapters {

    private static final Logger log = LoggerFactory.getLogger(DeviceAdapters.class);

    /** 车牌道闸：下发抬杆 / 黑白名单。 */
    public interface LprGateAdapter {
        void openGate(Long gateId, String plate);
    }

    /** 人脸设备：下发人员到设备人脸库。 */
    public interface FaceDeviceAdapter {
        void pushPerson(String deviceNo, String personId, String faceUrl);
    }

    /** 抓拍摄像头：抓图存证，返回图片URL。 */
    public interface CameraAdapter {
        String snapshot(String deviceNo, String bizTag);
    }

    /** 身份证 OCR。 */
    public interface OcrAdapter {
        Map<String, String> recognizeIdCard(String imageUrl);
    }

    /** 电子签：创建签署任务，返回签署链接。 */
    public interface ESignAdapter {
        String createSign(String subject, String signerName, String signerMobile);
    }

    @Component
    @ConditionalOnProperty(name = "fv.device.lpr", havingValue = "mock", matchIfMissing = true)
    public static class MockLprGate implements LprGateAdapter {
        public void openGate(Long gateId, String plate) {
            log.info("[车牌道闸-MOCK] 门岗 {} 识别 {} · 抬杆放行", gateId, plate);
        }
    }

    @Component
    public static class MockFaceDevice implements FaceDeviceAdapter {
        public void pushPerson(String deviceNo, String personId, String faceUrl) {
            log.info("[人脸设备-MOCK] 设备 {} 下发人员 {}", deviceNo, personId);
        }
    }

    @Component
    public static class MockCamera implements CameraAdapter {
        public String snapshot(String deviceNo, String bizTag) {
            String url = "https://minio.local/snapshots/" + bizTag + "-" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
            log.info("[抓拍-MOCK] 设备 {} 抓图 {} -> {}", deviceNo, bizTag, url);
            return url;
        }
    }

    @Component
    @ConditionalOnProperty(name = "fv.device.ocr", havingValue = "mock", matchIfMissing = true)
    public static class MockOcr implements OcrAdapter {
        public Map<String, String> recognizeIdCard(String imageUrl) {
            log.info("[OCR-MOCK] 识别身份证 {}", imageUrl);
            return Map.of("name", "张三", "idNo", "44190019900101****", "valid", "2020-2040");
        }
    }

    @Component
    @ConditionalOnProperty(name = "fv.device.esign", havingValue = "mock", matchIfMissing = true)
    public static class MockESign implements ESignAdapter {
        public String createSign(String subject, String signerName, String signerMobile) {
            String url = "https://esign.local/sign/" + UUID.randomUUID().toString().substring(0, 10);
            log.info("[电子签-MOCK] 为 {} 创建《{}》签署任务 -> {}", signerName, subject, url);
            return url;
        }
    }
}
