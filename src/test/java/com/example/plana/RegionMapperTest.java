package com.example.plana;

import com.example.plana.dto.RegionDto;
import com.example.plana.model.RegionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RegionMapperTest {

    @Autowired
    private RegionMapper regionMapper;

    @Test
    void selectAllRegions_테스트() {
        List<RegionDto> regions = regionMapper.selectAllRegions();

        assertThat(regions).isNotNull();
        assertThat(regions).isNotEmpty();

        System.out.println("총 행 수: " + regions.size());
        regions.forEach(r ->
                System.out.println(r.getRegionId() + " | " + r.getZdoName() + " | " + r.getSiguName())
        );
    }
}