package com.hotelfo.scanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoomRequest {

    @NotBlank(message = "Nomor kamar wajib diisi")
    @Size(max = 10, message = "Nomor kamar maksimal 10 karakter")
    private String roomNumber;
}
