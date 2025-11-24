package com.one.aim.bo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "address")
public class AddressBO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullName;

    private String street;

    private String city;

    private String state;

    private String zip;

    private String country;

    private String phone;

    private Long userid;

    @Column(name = "is_default")
    private Boolean isDefault = false;

}
