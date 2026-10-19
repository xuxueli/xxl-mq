package com.xxl.mq.admin.business.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class MessageMapperSqlBindingTest {

    @Test
    void pullQueryByUuidBindsWithTheDeclaredMapperParameters() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/business/MessageMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).as(resource).isNotNull();
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }

        MapperMethod.ParamMap<Object> parameters = new MapperMethod.ParamMap<>();
        parameters.put("messageIdList", List.of(1L));
        parameters.put("instanceUuid", "consumer-instance");
        parameters.put("status", 1);

        MappedStatement statement = configuration.getMappedStatement(
                "com.xxl.mq.admin.business.mapper.MessageMapper.pullQueryByUuid");
        BoundSql boundSql = statement.getBoundSql(parameters);

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        assertThatCode(() -> new DefaultParameterHandler(statement, parameters, boundSql)
                .setParameters(preparedStatement)).doesNotThrowAnyException();
    }
}
