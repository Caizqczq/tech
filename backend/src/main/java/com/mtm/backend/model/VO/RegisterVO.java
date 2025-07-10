package com.mtm.backend.model.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterVO {
    private Integer id;
    private String username;
    private String email;
    private Date createdAt;
}
