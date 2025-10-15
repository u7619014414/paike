package com.yl.paike.teacher.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> content;
    private Long totalElements;
    // 改为 long，兼容不同处传 long/int
    private long totalPages;
    private Integer page;
    private Integer size;
}
