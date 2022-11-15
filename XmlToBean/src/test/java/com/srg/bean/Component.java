package com.srg.bean;

import com.srg.AbstractBaseBean;
import lombok.Data;

/**
 * @author : SRG
 * @create : 2022/3/7
 * @describe :
 **/


@Data
public class Component extends AbstractBaseBean {

    private Double spotPrice;

    private Double allIn;

}
