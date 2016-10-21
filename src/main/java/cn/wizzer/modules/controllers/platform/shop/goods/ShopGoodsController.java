package cn.wizzer.modules.controllers.platform.shop.goods;

import cn.wizzer.common.annotation.SLog;
import cn.wizzer.common.base.Result;
import cn.wizzer.common.filter.PrivateFilter;
import cn.wizzer.common.page.DataTableColumn;
import cn.wizzer.common.page.DataTableOrder;
import cn.wizzer.modules.models.shop.Shop_goods;
import cn.wizzer.modules.models.shop.Shop_goods_type_paramg;
import cn.wizzer.modules.models.shop.Shop_goods_type_props;
import cn.wizzer.modules.services.shop.goods.*;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@IocBean
@At("/platform/shop/goods/goods")
@Filters({@By(type = PrivateFilter.class)})
public class ShopGoodsController {
    private static final Log log = Logs.get();
    @Inject
    private ShopGoodsService shopGoodsService;
    @Inject
    private ShopGoodsTypeService shopGoodsTypeService;
    @Inject
    private ShopGoodsClassService shopGoodsClassService;
    @Inject
    private ShopGoodsSpecService shopGoodsSpecService;
    @Inject
    private ShopGoodsTypeParamgService shopGoodsTypeParamgService;
    @Inject
    private ShopGoodsTypePropsService shopGoodsTypePropsService;
    @Inject
    private ShopMemberLvService shopMemberLvService;

    @At("")
    @Ok("beetl:/platform/shop/goods/goods/index.html")
    @RequiresAuthentication
    public void index(HttpServletRequest req) {

    }

    @At("/getClass/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getClass(String id) {
        return Result.success("", shopGoodsClassService.fetchLinks(shopGoodsClassService.fetch(id), "goodsType"));
    }

    @At("/getType/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getType(String id) {
        return Result.success("", shopGoodsTypeService.fetch(id));
    }

    @At("/getProps/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getProps(String id) {
        List<Shop_goods_type_props> list = shopGoodsTypePropsService.query(Cnd.where("typeId", "=", id));
        for (Shop_goods_type_props props : list) {
            shopGoodsTypePropsService.fetchLinks(props, "propsValues");
        }
        return Result.success("", list);
    }

    @At("/getParam/?")
    @Ok("json")
    @RequiresAuthentication
    public Object getParam(String id) {
        List<Shop_goods_type_paramg> list = shopGoodsTypeParamgService.query(Cnd.where("typeId", "=", id));
        for (Shop_goods_type_paramg paramg : list) {
            shopGoodsTypeParamgService.fetchLinks(paramg, "params");
        }
        return Result.success("", list);
    }

    @At
    @Ok("json:full")
    @RequiresAuthentication
    public Object data(@Param("length") int length, @Param("start") int start, @Param("draw") int draw, @Param("::order") List<DataTableOrder> order, @Param("::columns") List<DataTableColumn> columns) {
        Cnd cnd = Cnd.NEW();
        return shopGoodsService.data(length, start, draw, order, columns, cnd, null);
    }

    @At
    @Ok("beetl:/platform/shop/goods/goods/add.html")
    @RequiresAuthentication
    public void add(HttpServletRequest req) {
        req.setAttribute("typeList", shopGoodsTypeService.query());
        req.setAttribute("lvList", shopMemberLvService.query());
    }

    @At
    @Ok("json")
    @RequiresPermissions("shop.goods.manager.goods.add")
    @SLog(tag = "新建商品", msg = "商品名称:${args[0].name}")
    public Object addDo(@Param("..") Shop_goods shopGoods, HttpServletRequest req) {
        try {
            shopGoodsService.insert(shopGoods);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }

    @At("/edit/?")
    @Ok("beetl:/platform/shop/goods/goods/edit.html")
    @RequiresAuthentication
    public Object edit(String id) {
        return shopGoodsService.fetch(id);
    }

    @At
    @Ok("json")
    @RequiresPermissions("shop.goods.manager.goods.edit")
    @SLog(tag = "修改商品", msg = "商品名称:${args[0].name}")
    public Object editDo(@Param("..") Shop_goods shopGoods, HttpServletRequest req) {
        try {

            shopGoods.setOpAt((int) (System.currentTimeMillis() / 1000));
            shopGoodsService.updateIgnoreNull(shopGoods);
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }


    @At({"/delete", "/delete/?"})
    @Ok("json")
    @RequiresPermissions("shop.goods.manager.goods.delete")
    @SLog(tag = "删除商品", msg = "ID:${args[2].getAttribute('id')}")
    public Object delete(String id, @Param("ids") String[] ids, HttpServletRequest req) {
        try {
            if (ids != null && ids.length > 0) {
                shopGoodsService.delete(ids);
                req.setAttribute("id", org.apache.shiro.util.StringUtils.toString(ids));
            } else {
                shopGoodsService.delete(id);
                req.setAttribute("id", id);
            }
            return Result.success("system.success");
        } catch (Exception e) {
            return Result.error("system.error");
        }
    }


}