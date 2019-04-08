package com.mp4analyzer.repository;

import com.mp4analyzer.model.ScreenGrabMeta;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ScreenGrabRepository {

    private static final Logger log = Logger.getLogger(ScreenGrabRepository.class.getName());

    public void updateScreenGrab(ScreenGrabMeta screenGrabMeta){
        log.info("***Updating screen grab information: " + screenGrabMeta);
    }

}
