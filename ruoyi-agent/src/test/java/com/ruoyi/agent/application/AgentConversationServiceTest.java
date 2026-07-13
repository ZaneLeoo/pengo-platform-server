package com.ruoyi.agent.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.ruoyi.agent.domain.AgentConversation;
import com.ruoyi.agent.domain.AgentMessage;
import com.ruoyi.agent.mapper.AgentConversationMapper;
import com.ruoyi.agent.mapper.AgentMessageMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgentConversationServiceTest {
    @Test
    void shouldCreateOwnedConversationWithDefaultState() {
        FakeConversationMapper conversationMapper = new FakeConversationMapper();
        AgentMessageMapper messageMapper = new EmptyMessageMapper();
        AgentConversationService service = new AgentConversationService(conversationMapper, messageMapper,
                new ConversationTitlePolicy());

        AgentConversation created = service.create(12L, "  请   分析销售数据  ", "admin");

        assertEquals(7L, created.getId());
        assertEquals("请 分析销售数据", conversationMapper.saved.getTitle());
        assertEquals("active", conversationMapper.saved.getStatus());
        assertEquals(0, conversationMapper.saved.getMessageCount());
        assertEquals(12L, conversationMapper.saved.getUserId());
    }

    private static class FakeConversationMapper implements AgentConversationMapper {
        private AgentConversation saved;
        public int insert(AgentConversation value) {
            saved = value;
            value.setId(7L);
            return 1;
        }
        public List<AgentConversation> selectByUserId(Long userId) {
            return Collections.emptyList();
        }
        public AgentConversation selectOwned(Long id, Long userId) {
            return null;
        }
        public int update(AgentConversation value) {
            return 1;
        }
        public int deleteOwned(Long id, Long userId) {
            return 1;
        }
    }

    private static class EmptyMessageMapper implements AgentMessageMapper {
        public int insert(AgentMessage value) {
            return 1;
        }
        public int update(AgentMessage value) {
            return 1;
        }
        public AgentMessage selectById(Long id) {
            return null;
        }
        public List<AgentMessage> selectByConversationId(Long id) {
            return Collections.emptyList();
        }
        public int deleteByConversationId(Long id) {
            return 0;
        }
    }
}
