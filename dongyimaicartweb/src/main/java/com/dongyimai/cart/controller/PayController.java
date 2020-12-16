package com.dongyimai.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.dongyimai.entity.Result;
import com.dongyimai.order.service.OrderService;
import com.dongyimai.pay.service.AliPayService;
import com.dongyimai.pojo.TbPayLog;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private AliPayService aliPayService;

    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative(){

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        System.out.println("no : " + payLog.getOutTradeNo() +"\t  total fee : " + payLog.getTotalFee());
        //订单流水号 来自于东易买项目
        //交易流水号 来自于阿里巴巴
        if(payLog != null){
            return aliPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else{
            return new HashMap();
        }

    }


    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){

        Result result = null;
        int x = 0;

        while(true){
            //调用查询接口
            Map<String, String> map = null;

            try {
                map = aliPayService.queryPayStatus(out_trade_no);

            } catch (Exception e1) {
                /*e1.printStackTrace();*/
                System.out.println("调用查询服务出错");
            }

            if(map==null){//出错
                result=new Result(false, "支付出错");
                break;
            }

            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_SUCCESS")){//如果成功
                result=new Result(true, "支付成功");
                //支付成功修改订单状态
                orderService.updateOrderStatus(out_trade_no, map.get("trade_no"));

                break;
            }

            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_CLOSED")){//如果成功
                result=new Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }

            if(map.get("tradestatus")!=null&&map.get("tradestatus").equals("TRADE_FINISHED")){//如果成功
                result=new Result(true, "交易结束，不可退款");
                break;
            }

            x++;
            if(x>=100){
                result = new Result(false,"二维码超时");
                break;
            }


            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

}
