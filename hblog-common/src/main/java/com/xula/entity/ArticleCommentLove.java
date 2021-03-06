package com.xula.entity;

import cn.assist.easydao.annotation.Temporary;
import cn.assist.easydao.pojo.BasePojo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


/**
 * 
 * 文章评论
 * @author xla
 * @email xla@yuelinghui.com
 * @date 2018-11-02 17:19:31
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ArticleCommentLove extends BasePojo {
    @Temporary
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	private Integer id;
	/**
	 * 点赞用户uid
	 */
	private Integer uid;
	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 评论编号
	 */
	private Integer acId;

	/**
	 * 热度
	 */
	private Integer fever;

}
