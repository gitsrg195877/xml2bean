package com.srg.bean;

import com.srg.AbstractBaseBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author : SRG
 * @create : 2022/3/25
 * @describe :
 **/

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Party extends AbstractBaseBean {

    public String[] childNodes = {"partyId","partyName"};

    private String partyId;

    private String partyName;

    private String idsche;

    private String testField;


}
