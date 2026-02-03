package com.example.ReservationApp.service.impl.supplier;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.example.ReservationApp.dto.ResponseDTO;
import com.example.ReservationApp.dto.supplier.SupplierDTO;
import com.example.ReservationApp.entity.supplier.Supplier;
import com.example.ReservationApp.enums.SupplierProductStatus;
import com.example.ReservationApp.enums.SupplierStatus;
import com.example.ReservationApp.exception.NotFoundException;
import com.example.ReservationApp.mapper.SupplierMapper;
import com.example.ReservationApp.repository.supplier.SupplierRepository;
import com.example.ReservationApp.exception.AlreadyExistException;
import com.example.ReservationApp.exception.CannotDeleteException;
import com.example.ReservationApp.service.supplier.SupplierService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 仕入先（Supplier）に関するサービス実装クラス。
 * 主な機能:
 * - 仕入先の追加、取得、更新、削除
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    /**
     * 新しい仕入先を追加する。
     *
     * @param supplierDTO 追加する仕入先情報を持つDTO
     * @return 追加された仕入先のDTOを含むレスポンス
     * @throws AlreadyExistException 名前または電話番号が既に存在する場合
     */
    @Override
    public ResponseDTO<SupplierDTO> addSupplier(SupplierDTO supplierDTO) {

        if (supplierRepository.existsByName(supplierDTO.getName())) {
            throw new AlreadyExistException("この仕入先名は既に存在しています");
        }

        if (supplierRepository.existsByContactInfo(supplierDTO.getContactInfo())) {
            throw new AlreadyExistException("この電話番号は既に登録されています");
        }
        Supplier supplier = supplierMapper.toEntity(supplierDTO);
        supplierRepository.save(supplier);
        return ResponseDTO.<SupplierDTO>builder()
                .status(HttpStatus.OK.value())
                .message("新しい仕入先の追加に成功しました")
                .data(supplierMapper.toDTO(supplier))
                .build();
    }

    /**
     * 全ての仕入先を取得する。
     *
     * @return 仕入先DTOのリストを含むレスポンス
     */
    @Override
    public ResponseDTO<List<SupplierDTO>> getAllSuppliers() {

        List<Supplier> suppliers = supplierRepository.findAllWithProducts();
        List<SupplierDTO> supplierDTOs = supplierMapper.toDTOList(suppliers);
        return ResponseDTO.<List<SupplierDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("全て仕入先の取得に成功しました")
                .data(supplierDTOs)
                .build();
    }

    /**
     * 指定したIDの仕入先を取得する。
     *
     * @param id 取得対象の仕入先ID
     * @return 指定IDの仕入先DTOを含むレスポンス
     * @throws NotFoundException 指定した仕入先が存在しない場合
     */
    @Override
    public ResponseDTO<SupplierDTO> getSupplierById(Long id) {

        Supplier supplier = supplierRepository.findSupplierWithProductsAndCategory(id)
                .orElseThrow(() -> new NotFoundException("この仕入先は存在していません"));
        SupplierDTO supplierDTO = supplierMapper.toDTO(supplier);
        return ResponseDTO.<SupplierDTO>builder()
                .status(HttpStatus.OK.value())
                .message("仕入先の取得に成功しました")
                .data(supplierDTO)
                .build();
    }

    /**
     * 指定したIDの仕入先情報を更新する。
     *
     * @param id          更新対象の仕入先ID
     * @param supplierDTO 更新情報を持つDTO
     * @return 更新後の仕入先DTOを含むレスポンス
     * @throws NotFoundException 指定した仕入先が存在しない場合
     */
    @Override
    public ResponseDTO<SupplierDTO> updateSupplier(Long id, SupplierDTO supplierDTO) {

        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("この仕入先は存在していません"));
        if (supplierDTO.getName() != null && !supplierDTO.getName().isBlank()) {
            existingSupplier.setName(supplierDTO.getName());
        }
        if (supplierDTO.getAddress() != null && !supplierDTO.getAddress().isBlank()) {
            existingSupplier.setAddress(supplierDTO.getAddress());
        }
        if (supplierDTO.getMail() != null && !supplierDTO.getMail().isBlank()) {
            existingSupplier.setMail(supplierDTO.getMail());
        }
        if (supplierDTO.getContactInfo() != null && !supplierDTO.getContactInfo().isBlank()) {
            existingSupplier.setContactInfo(supplierDTO.getContactInfo());
        }
        if (supplierDTO.getSupplierStatus() != null) {
            existingSupplier.setStatus(supplierDTO.getSupplierStatus());
            if (supplierDTO.getSupplierStatus().equals(SupplierStatus.INACTIVE)) {
                existingSupplier.getSupplierProducts().forEach(sp -> sp.setStatus(SupplierProductStatus.INACTIVE));
            }
        }
        Supplier updatedSupplier = supplierRepository.save(existingSupplier);
        return ResponseDTO.<SupplierDTO>builder()
                .status(HttpStatus.OK.value())
                .message("仕入先の更新に成功しました")
                .data(supplierMapper.toDTO(updatedSupplier))
                .build();
    }

    /**
     * 指定したIDの仕入先を削除する。
     *
     * @param id 削除対象の仕入先ID
     * @return 成功メッセージを含むレスポンス
     * @throws NotFoundException 指定した仕入先が存在しない場合
     */
    @Override
    public ResponseDTO<Void> deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new NotFoundException("この仕入先は存在していません");
        }

        try {
            supplierRepository.deleteById(id);
            // ★ flush() を呼び出すことで、DELETE SQLを即時DBに送信する
            // flushしない場合、トランザクションcommit時にSQLが実行され、
            // DataIntegrityViolationExceptionをここでcatchできない
            supplierRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new CannotDeleteException(
                    "商品情報が存在するため、仕入先を削除できません");
        }
        return ResponseDTO.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("仕入先の削除に成功しました")
                .build();
    }

}
