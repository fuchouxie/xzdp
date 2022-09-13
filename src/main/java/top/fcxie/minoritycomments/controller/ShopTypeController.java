package top.fcxie.minoritycomments.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.service.IShopTypeService;

import javax.annotation.Resource;


/**
 * @version V1.0
 * @author fuchouxie
 * @description: 店铺类别控制器
 * @createDate 2022/9/5
 */

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService shopTypeService;

    @GetMapping("list")
    public Result list(){
        return shopTypeService.lists();
    }

}
