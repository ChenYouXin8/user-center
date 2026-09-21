// @ts-ignore
/* eslint-disable */
import request from 'umi-request';

/**
 * 适配本地 user-center 后端（Spring Boot）
 * 后端统一返回 { success, code, message, data }，成功 code = 0
 * 后端字段：username / avatarUrl / isValid；前端页面字段：userAccount / userAvatar / userStatus
 */

/** 读取本地保存的 JWT，组装 Authorization 头 */
function authHeader(): { [key: string]: string } {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

/** 将后端用户对象映射为前端 CurrentUser 结构 */
function adaptUser(u: any): API.CurrentUser {
  if (!u) return u;
  return {
    ...u,
    userAvatar: u.userAvatar ?? u.avatarUrl,
    userStatus: u.userStatus ?? (u.isValid === 1 ? 0 : 1),
    userAccount: u.userAccount ?? u.email ?? u.username,
  };
}

/** 获取当前的用户 GET /api/users/me */
export async function currentUser(options?: { [key: string]: any }) {
  const res = await request<API.BaseResponse<API.CurrentUser>>('/api/users/me', {
    method: 'GET',
    headers: { ...authHeader() },
    ...(options || {}),
  });
  return { ...res, data: adaptUser(res.data) };
}

/** 退出登录接口 POST /api/users/logout */
export async function outLogin(options?: { [key: string]: any }) {
  return request<API.BaseResponse<number>>('/api/users/logout', {
    method: 'POST',
    headers: { ...authHeader() },
    ...(options || {}),
  });
}

/** 登录接口 POST /api/users/login（前端字段 userAccount/userPassword -> 后端 username/password） */
export async function login(body: API.LoginParams, options?: { [key: string]: any }) {
  const { userAccount, userPassword, ...rest } = body || {};
  return request<API.BaseResponse<API.LoginResult>>('/api/users/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: { username: userAccount, password: userPassword, ...rest },
    ...(options || {}),
  });
}

/** 注册接口 POST /api/users/register（userAccount 作为邮箱，checkPassword -> confirmPassword） */
export async function register(body: API.RegisterParams, options?: { [key: string]: any }) {
  const { username, userAccount, userPassword, checkPassword, ...rest } = body || {};
  return request<API.BaseResponse<API.RegisterResult>>('/api/users/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: {
      username,
      email: userAccount,
      password: userPassword,
      confirmPassword: checkPassword,
      ...rest,
    },
    ...(options || {}),
  });
}

/** 用户列表 GET /api/users（分页，取 records 并映射字段） */
export async function searchUsers(options?: { [key: string]: any }) {
  const res = await request<API.BaseResponse<any>>('/api/users', {
    method: 'GET',
    headers: { ...authHeader() },
    params: { pageNum: 1, pageSize: 100 },
    ...(options || {}),
  });
  const records = res?.data?.records ?? res?.data ?? [];
  return { ...res, data: (records as any[]).map(adaptUser) };
}

/** 此处后端没有提供注释 GET /api/notices */
export async function getNotices(options?: { [key: string]: any }) {
  return request<API.NoticeIconList>('/api/notices', {
    method: 'GET',
    ...(options || {}),
  });
}

/** 获取规则列表 GET /api/rule */
export async function rule(
  params: {
    // query
    /** 当前的页码 */
    current?: number;
    /** 页面的容量 */
    pageSize?: number;
  },
  options?: { [key: string]: any },
) {
  return request<API.RuleList>('/api/rule', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** 新建规则 PUT /api/rule */
export async function updateRule(options?: { [key: string]: any }) {
  return request<API.RuleListItem>('/api/rule', {
    method: 'PUT',
    ...(options || {}),
  });
}

/** 新建规则 POST /api/rule */
export async function addRule(options?: { [key: string]: any }) {
  return request<API.RuleListItem>('/api/rule', {
    method: 'POST',
    ...(options || {}),
  });
}

/** 删除规则 DELETE /api/rule */
export async function removeRule(options?: { [key: string]: any }) {
  return request<Record<string, any>>('/api/rule', {
    method: 'DELETE',
    ...(options || {}),
  });
}
