package fast.campus.netplix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 스케줄링 활성화
public class NetplixBatchApplication {
    public static void main(String[] args) {
        // 스케줄러가 계속 실행되도록 exit 제거
        SpringApplication.run(NetplixBatchApplication.class, args);
    }
}
