package top.fcxie.minoritycomments.dto;

import lombok.Data;

/**
 * @version V1.0
 * @author fuchouxie
 * @description: 用户可展示数据
 * @createDate 2022/9/3
 */

@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
}
