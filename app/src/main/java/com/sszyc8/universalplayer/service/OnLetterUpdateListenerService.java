package com.sszyc8.universalplayer.service;

/**
 * 项目工程名: 快速索引
 * 作者：      sszyc8
 * 创建日期：  2016-01-13 16:32
 * 描述:      字母更新service
 * --------------------------------------------
 * 修改者：      sszyc8
 * 修改日期:     2016-01-13 16:32
 * 修改原因描述:
 * 版本号:       1.0
 */
public interface OnLetterUpdateListenerService {

    /**
     * 更新字母
     *
     * @param letter 字母
     */
    void onLetterUpdate(String letter);
}
