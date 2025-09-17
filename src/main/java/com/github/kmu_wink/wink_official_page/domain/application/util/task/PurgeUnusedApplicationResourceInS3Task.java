package com.github.kmu_wink.wink_official_page.domain.application.util.task;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.github.kmu_wink.wink_official_page.domain.application.repository.ApplicationRepository;
import com.github.kmu_wink.wink_official_page.domain.application.schema.Application;
import com.github.kmu_wink.wink_official_page.global.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Profile("prod")
@Component
@RequiredArgsConstructor
public class PurgeUnusedApplicationResourceInS3Task {

    private final ApplicationRepository applicationRepository;
    private final S3Service s3Service;

    // @EventListener(ApplicationReadyEvent.class)
    // @Scheduled(cron = "0 0 6 * * *")
    private void run() {

        Set<String> use = applicationRepository.findAll()
                .stream()
                .map(Application::getImg)
                .map(s3Service::urlToKey)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        AtomicInteger size = new AtomicInteger(0);

		/*
		  사진 업로드 후, 해당 URL을 클라이언트에서 서버로 넘겨 사진을 등록하는 형식임.
		  만약 12시 이전에 사진을 미리 업로드 후, 12시 이후에 등록 버튼을 누른다면, 위 작업에 의해 사진은 사라져있음.
		  따라서 만약 파일이 등록된 지 30분이 지나지 않았다면, 목록에서 제외함.
		 */
        s3Service.files("application")
                .stream()
                .filter(file -> System.currentTimeMillis() - file.getLastModified().getTime() > 1000 * 60 * 30)
                .map(S3ObjectSummary::getKey)
                .filter(file -> !use.contains(file))
                .peek((file) -> size.getAndAdd(1))
                .peek(file -> log.info("Purge {}", file))
                .forEach(s3Service::delete);

        log.info("Purge Unused Application Resource In S3. (amount: {})", size.get());
    }
}
