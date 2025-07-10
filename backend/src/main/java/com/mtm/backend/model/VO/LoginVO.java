package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private Integer id;
    private String username;
    private String email;
    private String avatar;
}
