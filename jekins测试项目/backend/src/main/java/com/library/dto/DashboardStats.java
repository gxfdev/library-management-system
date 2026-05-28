package com.library.dto;

import lombok.Data;

@Data
public class DashboardStats {

    private Long totalBooks;
    private Long totalUsers;
    private Long todayBorrows;
    private Long overdueCount;
    private Long totalBorrows;
    private Long availableBooks;
}
