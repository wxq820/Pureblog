package com.pureblog.widget.service;

import com.pureblog.widget.dto.AccountCategoryCreateDTO;
import com.pureblog.widget.dto.AccountCreateDTO;
import com.pureblog.widget.dto.NetWorthSnapshotCreateDTO;
import com.pureblog.widget.entity.AccountCategoryDO;
import com.pureblog.widget.entity.AccountDO;
import com.pureblog.widget.vo.AccountCategoryVO;
import com.pureblog.widget.vo.AccountVO;
import com.pureblog.widget.vo.NetWorthEntryVO;
import com.pureblog.widget.vo.NetWorthSnapshotVO;

import java.util.List;

public interface NetWorthService {

    // === 登录态 ===
    NetWorthSnapshotVO saveSnapshot(NetWorthSnapshotCreateDTO dto);

    List<NetWorthSnapshotVO> listSnapshots();

    List<NetWorthEntryVO> listEntries(Long snapshotId);

    NetWorthSnapshotVO latest();

    void deleteSnapshot(Long id);

    // === Admin: 分类 ===
    List<AccountCategoryVO> listAllCategories();

    AccountCategoryDO createCategory(AccountCategoryCreateDTO dto);

    void updateCategory(Long id, AccountCategoryCreateDTO dto);

    /** seed 3 个分类 (id 1/2/3) 不可删 */
    void deleteCategory(Long id);

    // === Admin: 账户 ===
    List<AccountVO> listAllAccounts();

    AccountDO createAccount(AccountCreateDTO dto);

    void updateAccount(Long id, AccountCreateDTO dto);

    void deleteAccount(Long id);
}
