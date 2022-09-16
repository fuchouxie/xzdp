package top.fcxie.minoritycomments.dto;

import lombok.Data;

import java.util.List;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 消息推送实体
 * @createDate 2022/9/16
 */

@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
