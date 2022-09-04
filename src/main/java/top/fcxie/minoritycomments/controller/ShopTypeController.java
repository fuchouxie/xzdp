package top.fcxie.minoritycomments.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.fcxie.minoritycomments.dto.Result;
import top.fcxie.minoritycomments.service.IShopTypeService;

import javax.annotation.Resource;

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
