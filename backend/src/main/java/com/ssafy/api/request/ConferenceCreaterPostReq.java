package com.ssafy.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 방 생성 API ([POST] /api/v1/users) 요청에 필요한 리퀘스트 바디 정의.
 */
@Getter
@Setter
@ApiModel("ConferenceRegisterPostRequest")
public class ConferenceCreaterPostReq {
	@ApiModelProperty(name="conferenceCategoryId", example="1")
	long conferenceCategoryId;
	@ApiModelProperty(name="title", example="방 제목")
	String title;
	@ApiModelProperty(name="description", example="방 설명")
	String description;
//	@ApiModelProperty(name="conferenceCategoryName", example="방 카테고리")
//	String conferenceCategoryName;
}