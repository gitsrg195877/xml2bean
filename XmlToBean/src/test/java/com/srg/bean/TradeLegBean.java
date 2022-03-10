package com.srg.bean;

import lombok.*;

/**
 * @author : SRG
 * @create : 2022/3/4
 * @describe :
 **/


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TradeLegBean {


    private String legType;

    private Integer amount;

    private String dealType;

    private Component component;

}
