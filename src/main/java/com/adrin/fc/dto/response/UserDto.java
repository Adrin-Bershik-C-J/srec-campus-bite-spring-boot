package com.adrin.fc.dto.response;

import com.adrin.fc.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String rollNumber;
    private String name;
    private Role role;
}
