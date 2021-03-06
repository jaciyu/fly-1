package com.xula.controller.article;

import cn.assist.easydao.common.Conditions;
import cn.assist.easydao.common.SqlExpr;
import cn.assist.easydao.common.SqlJoin;
import cn.assist.easydao.pojo.PagePojo;
import cn.assist.easydao.pojo.RecordPojo;
import com.alibaba.fastjson.JSONObject;
import com.xula.base.auth.Login;
import com.xula.base.constant.ArticleConstant;
import com.xula.base.constant.GlobalConstant;
import com.xula.base.constant.PageConstant;
import com.xula.base.utils.JsonBean;
import com.xula.base.utils.RecordBean;
import com.xula.base.utils.WebReqUtils;
import com.xula.controller.WebController;
import com.xula.entity.Article;
import com.xula.entity.Category;
import com.xula.entity.MemberArticle;
import com.xula.entity.extend.ArticleList;
import com.xula.entity.extend.CommentList;
import com.xula.event.AccessArticleEvent;
import com.xula.event.EventModel;
import com.xula.service.article.IArticleCategoryService;
import com.xula.service.article.IArticleService;
import com.xula.service.article.ICommentService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 文章的操作
 * @author xla
 */
@Controller
@RequestMapping({"/article","/column"})
public class ArticleController extends WebController {

    @Autowired
    private IArticleService iArticleService;
    @Autowired
    private IArticleCategoryService iArticleCategoryService;
    @Autowired
    private ICommentService iCommentService;

    /**
     * 添加文章
     * @return
     */
    @Login
    @PostMapping(value = "/add")
    @ResponseBody
    public JSONObject add(HttpServletRequest request) {
        Integer catId = WebReqUtils.getParamToInteger(request,"catId",null);
        String title = WebReqUtils.getParam(request,"title",null);
        String info = WebReqUtils.getParam(request,"info",null);
        Integer payPoint = WebReqUtils.getParamToInteger(request,"payPoint",null);
        String vcode = WebReqUtils.getParam(request,"vcode",null);
        if (StringUtils.isEmpty(vcode)) {
            return JsonBean.error("验证码必填！");
        }
        String code = request.getSession().getAttribute(GlobalConstant.CODE_NAME).toString();
        if (!vcode.equalsIgnoreCase(code)) {
            return JsonBean.error("验证码错误！");
        }
        if (catId == null) {
            return JsonBean.error("文章类型不能为空！");
        }
        if (StringUtils.isEmpty(title)) {
            return JsonBean.error("文章标题不能为空！");
        }
        if (StringUtils.isEmpty(info)) {
            return JsonBean.error("文章内容不能为空！");
        }
        if (payPoint == null) {
            return JsonBean.error("悬赏飞吻不能为空！");
        }
        RecordBean<Article> result = iArticleService.add(catId,title,info,payPoint);
        if (result.isSuccessCode()) {
            return JsonBean.success(result.getMsg(), PageConstant.INDEX);
        }
        return JsonBean.error(result.getMsg());
    }

    /**
     * 跳转添加文章页面
     * @return
     */
    @Login
    @GetMapping(value = "/add")
    public String addView() {
        return "article/add";
    }

    /**
     * 跳转修改文章页面
     * @return
     */
    @Login
    @GetMapping(value = "/edit/{aid}")
    public String editView(@PathVariable("aid") String aid,Model model) {
        if (StringUtils.isEmpty(aid)) {
            return renderTips(model,"文章编号不能为空！");
        }
        Article article = iArticleService.getArticle(aid);
        model.addAttribute("data",article);
        return "article/edit";
    }

    /**
     * 添加文章
     * @return
     */
    @Login
    @PostMapping(value = "/edit")
    @ResponseBody
    public JSONObject edit(HttpServletRequest request) {
        String aid = WebReqUtils.getParam(request,"aid",null);
        String title = WebReqUtils.getParam(request,"title",null);
        String info = WebReqUtils.getParam(request,"info",null);
        String vcode = WebReqUtils.getParam(request,"vcode",null);
        if (StringUtils.isEmpty(vcode)) {
            return JsonBean.error("验证码必填！");
        }
        String code = request.getSession().getAttribute(GlobalConstant.CODE_NAME).toString();
        if (!vcode.equalsIgnoreCase(code)) {
            return JsonBean.error("验证码错误！");
        }
        if (StringUtils.isEmpty(title)) {
            return JsonBean.error("文章标题不能为空！");
        }
        if (StringUtils.isEmpty(info)) {
            return JsonBean.error("文章内容不能为空！");
        }
        RecordBean<Article> result = iArticleService.update(aid,title,info);
        if (result.isSuccessCode()) {
            return JsonBean.success(result.getMsg(), PageConstant.INDEX);
        }
        return JsonBean.error(result.getMsg());
    }

    /**
     * 获取到文章列表页
     * @return
     */
    @GetMapping(value = {"/list/all","/list/all/page/{pageNo}"})
    public String index(@PathVariable(required = false) Integer pageNo,Model model) {
        // 获取 filter 列表数据
        Map<String,Object> map = new HashMap<>();
        // 获取 filter 列表数据
        map.put("filter","");
        map.put("type","all");
        PagePojo<ArticleList> page = iArticleService.getArticlePage(new Conditions(),getPageNo(pageNo), GlobalConstant.PAGE_SIZE);
        model.addAttribute("page",page);
        model.addAttribute("data",map);
        return "/article/index";
    }



    /**
     * 获取到文章列表页
     * @return
     */
    @GetMapping(value = {"/list/{typeName}","/list/{typeName}/{statusName}","/list/{typeName}/{statusName}/page/{pageNo}",})
    public String index(@PathVariable String typeName,@PathVariable(required = false) String statusName,@PathVariable(required = false) Integer pageNo,Model model) {
        // 获取 filter 列表数据
        Map<String,Object> map = new HashMap<>();
        map.put("filter",statusName);
        map.put("type",typeName);
        PagePojo<ArticleList> page = iArticleService.getArticlePage(getConditions(typeName,statusName),getPageNo(pageNo),GlobalConstant.PAGE_SIZE);

        model.addAttribute("page",page);
        model.addAttribute("data",map);
        return "/article/index";
    }


    /**
     * 跳转添加文章页面
     * @return
     */
    @GetMapping(value = {"/detail/{aid}","/detail/{aid}/page/{pageNo}/"})
    public String detail(@PathVariable("aid") String aid,@PathVariable(value = "pageNo",required = false) Integer pageNo,Model model) {
        RecordPojo recordPojo = iArticleService.getArticleInfo(aid);
        model.addAttribute("data",recordPojo.getColumns());
        pageNo = pageNo == null? 1:pageNo;
        PagePojo<CommentList> page = iCommentService.getCommentPage(new Conditions("a.article_id",SqlExpr.EQUAL,aid),pageNo,GlobalConstant.PAGE_SIZE);
        model.addAttribute("comment", page);

        //发布浏览事件
        Integer uid = WebReqUtils.getSessionUid(request);
        EventModel eventModel = new EventModel();
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("uid",uid);
        param.put("aid", aid);
        param.put("ip", WebReqUtils.getIp(request));
        eventModel.setParam(param);
        ac.publishEvent(new AccessArticleEvent(eventModel));

        return "article/detail";
    }

    /**
     * article/list/all
     * article/list/all/page/2
     * article/list/all/unsolved/page/2
     * article/list/all/solved
     * article/list/quiz
     *
     */


    /**
     * 根据typeName，获取筛选条件(目前先这样，后期有好方法再优化)
     * @param typeName
     * @return
     */
    private Conditions getConditions(String typeName,String statusName) {
        Category category = iArticleCategoryService.getCategoryByAlias(typeName);
        Conditions con = new Conditions("a.cat_id",SqlExpr.EQUAL,category.getId());
        if (StringUtils.isEmpty(statusName)) {
            return con;
        }
        if (statusName.equalsIgnoreCase(ArticleConstant.UNSOLVED)) {
            con.add(new Conditions("a.status", SqlExpr.EQUAL,ArticleConstant.STATUS_AUDITED), SqlJoin.AND);
        } else if (statusName.equalsIgnoreCase(ArticleConstant.SOLVED)) {
            con.add(new Conditions("a.status", SqlExpr.EQUAL,ArticleConstant.STATUS_SOLVED),SqlJoin.AND);
        } else {
            con.add(new Conditions("a.is_good", SqlExpr.EQUAL,1),SqlJoin.AND);
        }
        return con;
    }
}
