package com.mhp.planner.Services;

import java.io.ByteArrayInputStream;

public interface StatisticsService {
    ByteArrayInputStream generatePDF(Long id);
}
