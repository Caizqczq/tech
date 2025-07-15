package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationVO<T> {
    
    /**
     * 数据列表
     */
    private List<T> items;
    
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页大小
     */
    private int size;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages;
    }
    
    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }
    
    /**
     * 是否为第一页
     */
    public boolean isFirst() {
        return page == 1;
    }
    
    /**
     * 是否为最后一页
     */
    public boolean isLast() {
        return page == totalPages;
    }
}