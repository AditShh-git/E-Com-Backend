package com.one.aim.rs;


import com.one.vm.core.BaseDataRs;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressDataRs extends BaseDataRs {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String fullName;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String country;
    private String phone;
    private Long userid;

    // Constructor with message only
    public AddressDataRs(String message) {
        super(message);
    }
}
