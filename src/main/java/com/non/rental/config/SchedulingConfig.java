package com.non.rental.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable Spring scheduling for RentCast cache refresh jobs.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}