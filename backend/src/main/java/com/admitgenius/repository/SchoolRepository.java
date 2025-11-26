package com.admitgenius.repository;

import com.admitgenius.model.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    List<School> findByRankingLessThanEqual(Integer maxRanking);

    List<School> findByAverageGPABetween(Double minGPA, Double maxGPA);

    List<School> findByAverageGMATBetween(Integer minGMAT, Integer maxGMAT);

    boolean existsByName(String name);

    Page<School> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT s FROM School s WHERE EXISTS (SELECT p FROM s.topPrograms p WHERE LOWER(p) LIKE LOWER(CONCAT('%', :programName, '%')))")
    Page<School> findByProgramName(@Param("programName") String programName, Pageable pageable);

    /**
     * 按地区筛选学校（location字段包含国家、州、城市信息）
     * 
     * @param location 地区信息
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    List<School> findByLocationContainingIgnoreCase(@Param("location") String location);

    @Query("SELECT s FROM School s WHERE LOWER(s.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<School> findByLocationContainingIgnoreCase(@Param("location") String location, Pageable pageable);

    /**
     * 通过学校专业表查找提供特定专业的学校
     * 
     * @param programName 专业名称
     * @param pageable    分页参数
     * @return 学校分页列表
     */
    @Query("SELECT DISTINCT s FROM School s WHERE s.id IN (SELECT sp.school.id FROM SchoolProgram sp WHERE LOWER(sp.name) LIKE LOWER(CONCAT('%', :programName, '%')))")
    Page<School> findBySchoolProgramName(@Param("programName") String programName, Pageable pageable);

    /**
     * 搜索学校（按名称、地区、描述）
     * 
     * @param query 搜索关键词
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<School> searchSchools(@Param("query") String query);

    /**
     * 获取所有不同的地区（提取location字段中的国家部分）
     * 
     * @return 地区列表
     */
    @Query("SELECT DISTINCT s.location FROM School s WHERE s.location IS NOT NULL")
    List<String> findDistinctLocations();

    /**
     * 按top programs筛选学校
     * 
     * @param program 专业名称
     * @return 学校列表
     */
    @Query("SELECT s FROM School s WHERE EXISTS (SELECT p FROM s.topPrograms p WHERE LOWER(p) LIKE LOWER(CONCAT('%', :program, '%')))")
    List<School> findByTopProgramsContainingIgnoreCase(@Param("program") String program);
}