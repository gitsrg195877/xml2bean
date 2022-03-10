package com.srg.bean;

import lombok.*;

import java.util.Date;
import java.util.List;

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
public class TradeBean {

    private Date tradeDate;

    private String tradeTime;

    private Integer amount;

    private String pair;

    private List<TradeLegBean> legs;

//    private TradeLegBean leg1;
//
//    private TradeLegBean leg2;

}
