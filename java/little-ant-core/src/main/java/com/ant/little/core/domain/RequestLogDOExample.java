package com.ant.little.core.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestLogDOExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RequestLogDOExample() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andOpenIdIsNull() {
            addCriterion("open_id is null");
            return (Criteria) this;
        }

        public Criteria andOpenIdIsNotNull() {
            addCriterion("open_id is not null");
            return (Criteria) this;
        }

        public Criteria andOpenIdEqualTo(String value) {
            addCriterion("open_id =", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotEqualTo(String value) {
            addCriterion("open_id <>", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdGreaterThan(String value) {
            addCriterion("open_id >", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdGreaterThanOrEqualTo(String value) {
            addCriterion("open_id >=", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdLessThan(String value) {
            addCriterion("open_id <", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdLessThanOrEqualTo(String value) {
            addCriterion("open_id <=", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdLike(String value) {
            addCriterion("open_id like", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotLike(String value) {
            addCriterion("open_id not like", value, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdIn(List<String> values) {
            addCriterion("open_id in", values, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotIn(List<String> values) {
            addCriterion("open_id not in", values, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdBetween(String value1, String value2) {
            addCriterion("open_id between", value1, value2, "openId");
            return (Criteria) this;
        }

        public Criteria andOpenIdNotBetween(String value1, String value2) {
            addCriterion("open_id not between", value1, value2, "openId");
            return (Criteria) this;
        }

        public Criteria andGmtCreateIsNull() {
            addCriterion("gmt_create is null");
            return (Criteria) this;
        }

        public Criteria andGmtCreateIsNotNull() {
            addCriterion("gmt_create is not null");
            return (Criteria) this;
        }

        public Criteria andGmtCreateEqualTo(Date value) {
            addCriterion("gmt_create =", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateNotEqualTo(Date value) {
            addCriterion("gmt_create <>", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateGreaterThan(Date value) {
            addCriterion("gmt_create >", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateGreaterThanOrEqualTo(Date value) {
            addCriterion("gmt_create >=", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateLessThan(Date value) {
            addCriterion("gmt_create <", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateLessThanOrEqualTo(Date value) {
            addCriterion("gmt_create <=", value, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateIn(List<Date> values) {
            addCriterion("gmt_create in", values, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateNotIn(List<Date> values) {
            addCriterion("gmt_create not in", values, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateBetween(Date value1, Date value2) {
            addCriterion("gmt_create between", value1, value2, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtCreateNotBetween(Date value1, Date value2) {
            addCriterion("gmt_create not between", value1, value2, "gmtCreate");
            return (Criteria) this;
        }

        public Criteria andGmtModifierIsNull() {
            addCriterion("gmt_modifier is null");
            return (Criteria) this;
        }

        public Criteria andGmtModifierIsNotNull() {
            addCriterion("gmt_modifier is not null");
            return (Criteria) this;
        }

        public Criteria andGmtModifierEqualTo(Date value) {
            addCriterion("gmt_modifier =", value, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierNotEqualTo(Date value) {
            addCriterion("gmt_modifier <>", value, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierGreaterThan(Date value) {
            addCriterion("gmt_modifier >", value, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierGreaterThanOrEqualTo(Date value) {
            addCriterion("gmt_modifier >=", value, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierLessThan(Date value) {
            addCriterion("gmt_modifier <", value, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierLessThanOrEqualTo(Date value) {
            addCriterion("gmt_modifier <=", value, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierIn(List<Date> values) {
            addCriterion("gmt_modifier in", values, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierNotIn(List<Date> values) {
            addCriterion("gmt_modifier not in", values, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierBetween(Date value1, Date value2) {
            addCriterion("gmt_modifier between", value1, value2, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andGmtModifierNotBetween(Date value1, Date value2) {
            addCriterion("gmt_modifier not between", value1, value2, "gmtModifier");
            return (Criteria) this;
        }

        public Criteria andEnvIsNull() {
            addCriterion("env is null");
            return (Criteria) this;
        }

        public Criteria andEnvIsNotNull() {
            addCriterion("env is not null");
            return (Criteria) this;
        }

        public Criteria andEnvEqualTo(String value) {
            addCriterion("env =", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvNotEqualTo(String value) {
            addCriterion("env <>", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvGreaterThan(String value) {
            addCriterion("env >", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvGreaterThanOrEqualTo(String value) {
            addCriterion("env >=", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvLessThan(String value) {
            addCriterion("env <", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvLessThanOrEqualTo(String value) {
            addCriterion("env <=", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvLike(String value) {
            addCriterion("env like", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvNotLike(String value) {
            addCriterion("env not like", value, "env");
            return (Criteria) this;
        }

        public Criteria andEnvIn(List<String> values) {
            addCriterion("env in", values, "env");
            return (Criteria) this;
        }

        public Criteria andEnvNotIn(List<String> values) {
            addCriterion("env not in", values, "env");
            return (Criteria) this;
        }

        public Criteria andEnvBetween(String value1, String value2) {
            addCriterion("env between", value1, value2, "env");
            return (Criteria) this;
        }

        public Criteria andEnvNotBetween(String value1, String value2) {
            addCriterion("env not between", value1, value2, "env");
            return (Criteria) this;
        }

        public Criteria andAppidIsNull() {
            addCriterion("appid is null");
            return (Criteria) this;
        }

        public Criteria andAppidIsNotNull() {
            addCriterion("appid is not null");
            return (Criteria) this;
        }

        public Criteria andAppidEqualTo(String value) {
            addCriterion("appid =", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidNotEqualTo(String value) {
            addCriterion("appid <>", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidGreaterThan(String value) {
            addCriterion("appid >", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidGreaterThanOrEqualTo(String value) {
            addCriterion("appid >=", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidLessThan(String value) {
            addCriterion("appid <", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidLessThanOrEqualTo(String value) {
            addCriterion("appid <=", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidLike(String value) {
            addCriterion("appid like", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidNotLike(String value) {
            addCriterion("appid not like", value, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidIn(List<String> values) {
            addCriterion("appid in", values, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidNotIn(List<String> values) {
            addCriterion("appid not in", values, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidBetween(String value1, String value2) {
            addCriterion("appid between", value1, value2, "appid");
            return (Criteria) this;
        }

        public Criteria andAppidNotBetween(String value1, String value2) {
            addCriterion("appid not between", value1, value2, "appid");
            return (Criteria) this;
        }

        public Criteria andRequestInfoIsNull() {
            addCriterion("request_info is null");
            return (Criteria) this;
        }

        public Criteria andRequestInfoIsNotNull() {
            addCriterion("request_info is not null");
            return (Criteria) this;
        }

        public Criteria andRequestInfoEqualTo(String value) {
            addCriterion("request_info =", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoNotEqualTo(String value) {
            addCriterion("request_info <>", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoGreaterThan(String value) {
            addCriterion("request_info >", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoGreaterThanOrEqualTo(String value) {
            addCriterion("request_info >=", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoLessThan(String value) {
            addCriterion("request_info <", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoLessThanOrEqualTo(String value) {
            addCriterion("request_info <=", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoLike(String value) {
            addCriterion("request_info like", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoNotLike(String value) {
            addCriterion("request_info not like", value, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoIn(List<String> values) {
            addCriterion("request_info in", values, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoNotIn(List<String> values) {
            addCriterion("request_info not in", values, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoBetween(String value1, String value2) {
            addCriterion("request_info between", value1, value2, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andRequestInfoNotBetween(String value1, String value2) {
            addCriterion("request_info not between", value1, value2, "requestInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoIsNull() {
            addCriterion("response_info is null");
            return (Criteria) this;
        }

        public Criteria andResponseInfoIsNotNull() {
            addCriterion("response_info is not null");
            return (Criteria) this;
        }

        public Criteria andResponseInfoEqualTo(String value) {
            addCriterion("response_info =", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoNotEqualTo(String value) {
            addCriterion("response_info <>", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoGreaterThan(String value) {
            addCriterion("response_info >", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoGreaterThanOrEqualTo(String value) {
            addCriterion("response_info >=", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoLessThan(String value) {
            addCriterion("response_info <", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoLessThanOrEqualTo(String value) {
            addCriterion("response_info <=", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoLike(String value) {
            addCriterion("response_info like", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoNotLike(String value) {
            addCriterion("response_info not like", value, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoIn(List<String> values) {
            addCriterion("response_info in", values, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoNotIn(List<String> values) {
            addCriterion("response_info not in", values, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoBetween(String value1, String value2) {
            addCriterion("response_info between", value1, value2, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andResponseInfoNotBetween(String value1, String value2) {
            addCriterion("response_info not between", value1, value2, "responseInfo");
            return (Criteria) this;
        }

        public Criteria andToUserNameIsNull() {
            addCriterion("to_user_name is null");
            return (Criteria) this;
        }

        public Criteria andToUserNameIsNotNull() {
            addCriterion("to_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andToUserNameEqualTo(String value) {
            addCriterion("to_user_name =", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameNotEqualTo(String value) {
            addCriterion("to_user_name <>", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameGreaterThan(String value) {
            addCriterion("to_user_name >", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("to_user_name >=", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameLessThan(String value) {
            addCriterion("to_user_name <", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameLessThanOrEqualTo(String value) {
            addCriterion("to_user_name <=", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameLike(String value) {
            addCriterion("to_user_name like", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameNotLike(String value) {
            addCriterion("to_user_name not like", value, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameIn(List<String> values) {
            addCriterion("to_user_name in", values, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameNotIn(List<String> values) {
            addCriterion("to_user_name not in", values, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameBetween(String value1, String value2) {
            addCriterion("to_user_name between", value1, value2, "toUserName");
            return (Criteria) this;
        }

        public Criteria andToUserNameNotBetween(String value1, String value2) {
            addCriterion("to_user_name not between", value1, value2, "toUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameIsNull() {
            addCriterion("from_user_name is null");
            return (Criteria) this;
        }

        public Criteria andFromUserNameIsNotNull() {
            addCriterion("from_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andFromUserNameEqualTo(String value) {
            addCriterion("from_user_name =", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameNotEqualTo(String value) {
            addCriterion("from_user_name <>", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameGreaterThan(String value) {
            addCriterion("from_user_name >", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("from_user_name >=", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameLessThan(String value) {
            addCriterion("from_user_name <", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameLessThanOrEqualTo(String value) {
            addCriterion("from_user_name <=", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameLike(String value) {
            addCriterion("from_user_name like", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameNotLike(String value) {
            addCriterion("from_user_name not like", value, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameIn(List<String> values) {
            addCriterion("from_user_name in", values, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameNotIn(List<String> values) {
            addCriterion("from_user_name not in", values, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameBetween(String value1, String value2) {
            addCriterion("from_user_name between", value1, value2, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andFromUserNameNotBetween(String value1, String value2) {
            addCriterion("from_user_name not between", value1, value2, "fromUserName");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Long value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Long value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Long value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Long value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Long value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Long> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Long> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Long value1, Long value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Long value1, Long value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIsNull() {
            addCriterion("msg_type is null");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIsNotNull() {
            addCriterion("msg_type is not null");
            return (Criteria) this;
        }

        public Criteria andMsgTypeEqualTo(String value) {
            addCriterion("msg_type =", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotEqualTo(String value) {
            addCriterion("msg_type <>", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeGreaterThan(String value) {
            addCriterion("msg_type >", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeGreaterThanOrEqualTo(String value) {
            addCriterion("msg_type >=", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeLessThan(String value) {
            addCriterion("msg_type <", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeLessThanOrEqualTo(String value) {
            addCriterion("msg_type <=", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeLike(String value) {
            addCriterion("msg_type like", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotLike(String value) {
            addCriterion("msg_type not like", value, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeIn(List<String> values) {
            addCriterion("msg_type in", values, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotIn(List<String> values) {
            addCriterion("msg_type not in", values, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeBetween(String value1, String value2) {
            addCriterion("msg_type between", value1, value2, "msgType");
            return (Criteria) this;
        }

        public Criteria andMsgTypeNotBetween(String value1, String value2) {
            addCriterion("msg_type not between", value1, value2, "msgType");
            return (Criteria) this;
        }

        public Criteria andContentIsNull() {
            addCriterion("content is null");
            return (Criteria) this;
        }

        public Criteria andContentIsNotNull() {
            addCriterion("content is not null");
            return (Criteria) this;
        }

        public Criteria andContentEqualTo(String value) {
            addCriterion("content =", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotEqualTo(String value) {
            addCriterion("content <>", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThan(String value) {
            addCriterion("content >", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThanOrEqualTo(String value) {
            addCriterion("content >=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThan(String value) {
            addCriterion("content <", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThanOrEqualTo(String value) {
            addCriterion("content <=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLike(String value) {
            addCriterion("content like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotLike(String value) {
            addCriterion("content not like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentIn(List<String> values) {
            addCriterion("content in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotIn(List<String> values) {
            addCriterion("content not in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentBetween(String value1, String value2) {
            addCriterion("content between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotBetween(String value1, String value2) {
            addCriterion("content not between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andWxSourceIsNull() {
            addCriterion("wx_source is null");
            return (Criteria) this;
        }

        public Criteria andWxSourceIsNotNull() {
            addCriterion("wx_source is not null");
            return (Criteria) this;
        }

        public Criteria andWxSourceEqualTo(String value) {
            addCriterion("wx_source =", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceNotEqualTo(String value) {
            addCriterion("wx_source <>", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceGreaterThan(String value) {
            addCriterion("wx_source >", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceGreaterThanOrEqualTo(String value) {
            addCriterion("wx_source >=", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceLessThan(String value) {
            addCriterion("wx_source <", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceLessThanOrEqualTo(String value) {
            addCriterion("wx_source <=", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceLike(String value) {
            addCriterion("wx_source like", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceNotLike(String value) {
            addCriterion("wx_source not like", value, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceIn(List<String> values) {
            addCriterion("wx_source in", values, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceNotIn(List<String> values) {
            addCriterion("wx_source not in", values, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceBetween(String value1, String value2) {
            addCriterion("wx_source between", value1, value2, "wxSource");
            return (Criteria) this;
        }

        public Criteria andWxSourceNotBetween(String value1, String value2) {
            addCriterion("wx_source not between", value1, value2, "wxSource");
            return (Criteria) this;
        }

        public Criteria andRealIpIsNull() {
            addCriterion("real_ip is null");
            return (Criteria) this;
        }

        public Criteria andRealIpIsNotNull() {
            addCriterion("real_ip is not null");
            return (Criteria) this;
        }

        public Criteria andRealIpEqualTo(String value) {
            addCriterion("real_ip =", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpNotEqualTo(String value) {
            addCriterion("real_ip <>", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpGreaterThan(String value) {
            addCriterion("real_ip >", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpGreaterThanOrEqualTo(String value) {
            addCriterion("real_ip >=", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpLessThan(String value) {
            addCriterion("real_ip <", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpLessThanOrEqualTo(String value) {
            addCriterion("real_ip <=", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpLike(String value) {
            addCriterion("real_ip like", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpNotLike(String value) {
            addCriterion("real_ip not like", value, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpIn(List<String> values) {
            addCriterion("real_ip in", values, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpNotIn(List<String> values) {
            addCriterion("real_ip not in", values, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpBetween(String value1, String value2) {
            addCriterion("real_ip between", value1, value2, "realIp");
            return (Criteria) this;
        }

        public Criteria andRealIpNotBetween(String value1, String value2) {
            addCriterion("real_ip not between", value1, value2, "realIp");
            return (Criteria) this;
        }

        public Criteria andMsgIdIsNull() {
            addCriterion("msg_id is null");
            return (Criteria) this;
        }

        public Criteria andMsgIdIsNotNull() {
            addCriterion("msg_id is not null");
            return (Criteria) this;
        }

        public Criteria andMsgIdEqualTo(Long value) {
            addCriterion("msg_id =", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdNotEqualTo(Long value) {
            addCriterion("msg_id <>", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdGreaterThan(Long value) {
            addCriterion("msg_id >", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdGreaterThanOrEqualTo(Long value) {
            addCriterion("msg_id >=", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdLessThan(Long value) {
            addCriterion("msg_id <", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdLessThanOrEqualTo(Long value) {
            addCriterion("msg_id <=", value, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdIn(List<Long> values) {
            addCriterion("msg_id in", values, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdNotIn(List<Long> values) {
            addCriterion("msg_id not in", values, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdBetween(Long value1, Long value2) {
            addCriterion("msg_id between", value1, value2, "msgId");
            return (Criteria) this;
        }

        public Criteria andMsgIdNotBetween(Long value1, Long value2) {
            addCriterion("msg_id not between", value1, value2, "msgId");
            return (Criteria) this;
        }

        public Criteria andEventIsNull() {
            addCriterion("event is null");
            return (Criteria) this;
        }

        public Criteria andEventIsNotNull() {
            addCriterion("event is not null");
            return (Criteria) this;
        }

        public Criteria andEventEqualTo(String value) {
            addCriterion("event =", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventNotEqualTo(String value) {
            addCriterion("event <>", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventGreaterThan(String value) {
            addCriterion("event >", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventGreaterThanOrEqualTo(String value) {
            addCriterion("event >=", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventLessThan(String value) {
            addCriterion("event <", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventLessThanOrEqualTo(String value) {
            addCriterion("event <=", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventLike(String value) {
            addCriterion("event like", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventNotLike(String value) {
            addCriterion("event not like", value, "event");
            return (Criteria) this;
        }

        public Criteria andEventIn(List<String> values) {
            addCriterion("event in", values, "event");
            return (Criteria) this;
        }

        public Criteria andEventNotIn(List<String> values) {
            addCriterion("event not in", values, "event");
            return (Criteria) this;
        }

        public Criteria andEventBetween(String value1, String value2) {
            addCriterion("event between", value1, value2, "event");
            return (Criteria) this;
        }

        public Criteria andEventNotBetween(String value1, String value2) {
            addCriterion("event not between", value1, value2, "event");
            return (Criteria) this;
        }

        public Criteria andEventKeyIsNull() {
            addCriterion("event_key is null");
            return (Criteria) this;
        }

        public Criteria andEventKeyIsNotNull() {
            addCriterion("event_key is not null");
            return (Criteria) this;
        }

        public Criteria andEventKeyEqualTo(String value) {
            addCriterion("event_key =", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyNotEqualTo(String value) {
            addCriterion("event_key <>", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyGreaterThan(String value) {
            addCriterion("event_key >", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyGreaterThanOrEqualTo(String value) {
            addCriterion("event_key >=", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyLessThan(String value) {
            addCriterion("event_key <", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyLessThanOrEqualTo(String value) {
            addCriterion("event_key <=", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyLike(String value) {
            addCriterion("event_key like", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyNotLike(String value) {
            addCriterion("event_key not like", value, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyIn(List<String> values) {
            addCriterion("event_key in", values, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyNotIn(List<String> values) {
            addCriterion("event_key not in", values, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyBetween(String value1, String value2) {
            addCriterion("event_key between", value1, value2, "eventKey");
            return (Criteria) this;
        }

        public Criteria andEventKeyNotBetween(String value1, String value2) {
            addCriterion("event_key not between", value1, value2, "eventKey");
            return (Criteria) this;
        }

        public Criteria andTicketIsNull() {
            addCriterion("ticket is null");
            return (Criteria) this;
        }

        public Criteria andTicketIsNotNull() {
            addCriterion("ticket is not null");
            return (Criteria) this;
        }

        public Criteria andTicketEqualTo(String value) {
            addCriterion("ticket =", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketNotEqualTo(String value) {
            addCriterion("ticket <>", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketGreaterThan(String value) {
            addCriterion("ticket >", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketGreaterThanOrEqualTo(String value) {
            addCriterion("ticket >=", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketLessThan(String value) {
            addCriterion("ticket <", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketLessThanOrEqualTo(String value) {
            addCriterion("ticket <=", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketLike(String value) {
            addCriterion("ticket like", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketNotLike(String value) {
            addCriterion("ticket not like", value, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketIn(List<String> values) {
            addCriterion("ticket in", values, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketNotIn(List<String> values) {
            addCriterion("ticket not in", values, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketBetween(String value1, String value2) {
            addCriterion("ticket between", value1, value2, "ticket");
            return (Criteria) this;
        }

        public Criteria andTicketNotBetween(String value1, String value2) {
            addCriterion("ticket not between", value1, value2, "ticket");
            return (Criteria) this;
        }

        public Criteria andLatitudeIsNull() {
            addCriterion("latitude is null");
            return (Criteria) this;
        }

        public Criteria andLatitudeIsNotNull() {
            addCriterion("latitude is not null");
            return (Criteria) this;
        }

        public Criteria andLatitudeEqualTo(Double value) {
            addCriterion("latitude =", value, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeNotEqualTo(Double value) {
            addCriterion("latitude <>", value, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeGreaterThan(Double value) {
            addCriterion("latitude >", value, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeGreaterThanOrEqualTo(Double value) {
            addCriterion("latitude >=", value, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeLessThan(Double value) {
            addCriterion("latitude <", value, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeLessThanOrEqualTo(Double value) {
            addCriterion("latitude <=", value, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeIn(List<Double> values) {
            addCriterion("latitude in", values, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeNotIn(List<Double> values) {
            addCriterion("latitude not in", values, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeBetween(Double value1, Double value2) {
            addCriterion("latitude between", value1, value2, "latitude");
            return (Criteria) this;
        }

        public Criteria andLatitudeNotBetween(Double value1, Double value2) {
            addCriterion("latitude not between", value1, value2, "latitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeIsNull() {
            addCriterion("longitude is null");
            return (Criteria) this;
        }

        public Criteria andLongitudeIsNotNull() {
            addCriterion("longitude is not null");
            return (Criteria) this;
        }

        public Criteria andLongitudeEqualTo(Double value) {
            addCriterion("longitude =", value, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeNotEqualTo(Double value) {
            addCriterion("longitude <>", value, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeGreaterThan(Double value) {
            addCriterion("longitude >", value, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeGreaterThanOrEqualTo(Double value) {
            addCriterion("longitude >=", value, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeLessThan(Double value) {
            addCriterion("longitude <", value, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeLessThanOrEqualTo(Double value) {
            addCriterion("longitude <=", value, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeIn(List<Double> values) {
            addCriterion("longitude in", values, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeNotIn(List<Double> values) {
            addCriterion("longitude not in", values, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeBetween(Double value1, Double value2) {
            addCriterion("longitude between", value1, value2, "longitude");
            return (Criteria) this;
        }

        public Criteria andLongitudeNotBetween(Double value1, Double value2) {
            addCriterion("longitude not between", value1, value2, "longitude");
            return (Criteria) this;
        }

        public Criteria andPrecisionIsNull() {
            addCriterion("precision is null");
            return (Criteria) this;
        }

        public Criteria andPrecisionIsNotNull() {
            addCriterion("precision is not null");
            return (Criteria) this;
        }

        public Criteria andPrecisionEqualTo(Double value) {
            addCriterion("precision =", value, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionNotEqualTo(Double value) {
            addCriterion("precision <>", value, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionGreaterThan(Double value) {
            addCriterion("precision >", value, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionGreaterThanOrEqualTo(Double value) {
            addCriterion("precision >=", value, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionLessThan(Double value) {
            addCriterion("precision <", value, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionLessThanOrEqualTo(Double value) {
            addCriterion("precision <=", value, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionIn(List<Double> values) {
            addCriterion("precision in", values, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionNotIn(List<Double> values) {
            addCriterion("precision not in", values, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionBetween(Double value1, Double value2) {
            addCriterion("precision between", value1, value2, "precision");
            return (Criteria) this;
        }

        public Criteria andPrecisionNotBetween(Double value1, Double value2) {
            addCriterion("precision not between", value1, value2, "precision");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}