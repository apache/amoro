<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
/ -->

<script lang="ts">
import { defineComponent, computed, ref } from 'vue'
import { Modal, Progress } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import type { IBaseDetailInfo } from '@/types/common.type'

interface SubModule {
  name: string
  label: string
  score: number | null
  max: number
  description: string
}

export default defineComponent({
  name: 'UHealthScore',
  components: {
    AModal: Modal,
    Progress
  },
  props: {
    baseInfo: {
      type: Object as () => IBaseDetailInfo,
      required: true
    }
  },
  setup(props) {
    const { t } = useI18n()
    const modalVisible = ref(false)

    const shouldShowDetails = computed(() => {
      return props.baseInfo.healthScore != null && props.baseInfo.healthScore > 0
    })

    const displayScore = computed(() => {
      return props.baseInfo.healthScore == null || props.baseInfo.healthScore < 0 ? 'N/A' : props.baseInfo.healthScore
    })

    // Calculate progress percentage
    const getPercent = (score: number | null, max: number) => {
      if (score == null || score <= 0) return 0
      return Math.min((score / max) * 100, 100)
    }

    // Format score for display
    const formatScore = (score: number | null) => {
      if (score == null || score <= 0) return 0
      return score
    }

    // Get sorted sub modules by score difference
    const sortedModules = computed(() => {
      const subModules: SubModule[] = [
        {
          name: 'smallFileScore',
          label: t('smallFileScore'),
          score: props.baseInfo.smallFileScore,
          max: 40,
          description: t('smallFileScoreDescription')
        },
        {
          name: 'equalityDeleteScore',
          label: t('equalityDeleteScore'),
          score: props.baseInfo.equalityDeleteScore,
          max: 40,
          description: t('equalityDeleteScoreDescription')
        },
        {
          name: 'positionalDeleteScore',
          label: t('positionalDeleteScore'),
          score: props.baseInfo.positionalDeleteScore,
          max: 20,
          description: t('positionalDeleteScoreDescription')
        }
      ]

      // Sort by difference between max score and actual score (largest difference first)
      return [...subModules].sort((a, b) => {
        // Handle null or negative scores as 0 (maximum difference)
        const diffA = (a.score == null || a.score < 0) ? a.max : a.max - a.score
        const diffB = (b.score == null || b.score < 0) ? b.max : b.max - b.score
        return diffB - diffA
      })
    })

    const showHealthScoreDetail = () => {
      // Only show modal when healthScore is greater than 0
      if (props.baseInfo.healthScore == null || props.baseInfo.healthScore <= 0) {
        return
      }

      modalVisible.value = true
    }

    const handleModalCancel = () => {
      modalVisible.value = false
    }

    return {
      shouldShowDetails,
      displayScore,
      showHealthScoreDetail,
      modalVisible,
      handleModalCancel,
      sortedModules,
      getPercent,
      formatScore,
      t
    }
  }
})
</script>

<template>
  <span
    :class="{'text-color': true, 'clickable-score': shouldShowDetails}"
    @click="showHealthScoreDetail"
  >
    {{ displayScore }}
  </span>

  <a-modal
    v-model:visible="modalVisible"
    :footer="null"
    class="health-score-modal"
    width="fit-content"
    :closable="true"
    centered
    @cancel="handleModalCancel"
  >
    <div class="health-score-modal-content">
      <h2 class="health-score-title">{{ t('healthScoreDetails') }}</h2>

      <div class="health-score-section">
        <div class="health-score-row">
          <div class="module-info">{{ t('healthScore') }}</div>
          <div class="module-score">{{ `${formatScore(baseInfo.healthScore)}/100` }}</div>
        </div>
        <div class="progress-bar-wrapper">
          <Progress
            :percent="baseInfo.healthScore != null ?
              Math.min(Math.max(baseInfo.healthScore, 0), 100) : 0"
            :show-info="false"
            stroke-color="#7CB305"
            :stroke-width="8"
          />
        </div>
        <div class="score-description">{{ t('healthScoreDescription') }}</div>
        <!-- Formula (internationalized) -->
        <div class="formula-row">
          {{ `${t('healthScore')} = ${t('smallFileScore')} + ${t('equalityDeleteScore')} + ${t('positionalDeleteScore')}` }}
        </div>
      </div>

      <h3 class="submodule-title">{{ t('subModule') }}</h3>

      <div class="submodule-section">
        <div
          v-for="module in sortedModules"
          :key="module.name"
          class="submodule-item"
        >
          <div class="submodule-row">
            <div class="module-info">{{ module.label }}</div>
            <div class="module-score">{{ `${formatScore(module.score)}/${module.max}` }}</div>
          </div>
          <div class="progress-bar-wrapper">
            <Progress
              :percent="getPercent(module.score, module.max)"
              :show-info="false"
              stroke-color="#1890ff"
              :stroke-width="8"
            />
          </div>
          <div class="score-description">{{ module.description }}</div>
        </div>
      </div>
    </div>
  </a-modal>
</template>

<style lang="less">
.health-score-modal {
  .ant-modal-content {
    padding: 0;
  }

  .ant-modal-confirm-content {
    margin: 0;
    padding: 0;
  }

  .ant-modal-body {
    padding: 0;

    // Hide scrollbar but keep functionality
    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: transparent;
      border-radius: 3px;
    }

    &::-webkit-scrollbar-thumb:hover {
      background: rgba(0, 0, 0, 0.2);
    }

    &:hover {
      &::-webkit-scrollbar-thumb {
        background: rgba(0, 0, 0, 0.1);
      }
    }
  }

  .health-score-modal-content {
    max-width: 500px;
    padding: 6px 12px; // Further reduce margins

    .health-score-title {
      text-align: center;
      font-weight: bold;
      font-size: 16px; // Same size as .attr-title in Details.vue
      color: #102048; // Same color as .attr-title in Details.vue
      margin: 0 0 6px 0; // Reduce margins
      padding: 0;
      line-height: 24px; // Same line height as .attr-title in Details.vue
    }

    .health-score-section {
      margin-bottom: 12px; // Increase margin to create 2x line spacing

      .health-score-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .module-info {
          font-weight: 500;
          color: #102048;
          font-size: 14px;
        }

        .module-score {
          font-size: 14px; // Unified font size
          color: #102048;
        }
      }

      .progress-bar-wrapper {
        margin-bottom: 4px;
        :deep(.ant-progress) {
          .ant-progress-outer {
            .ant-progress-inner {
              border-radius: 4px;
              overflow: hidden;
            }
          }
        }
      }

      .formula-row {
        margin: 4px 0;
        padding: 4px 6px;
        background-color: #f9f9f9;
        border-radius: 4px;
        font-family: monospace;
        white-space: nowrap;
        overflow: hidden;
        color: #102048;
        text-overflow: ellipsis;
        font-size: 12px; // Keep small font to fit space
        text-align: center;
      }

      .score-description {
        font-size: 12px; // Keep small font
        color: #102048;
        line-height: 1.4;
        margin: 4px 0;
      }
    }

    .submodule-title {
      font-weight: bold;
      text-align: left;
      margin: 12px 0 8px 0; // Increase top margin to create 2x line spacing
      font-size: 14px; // Keep consistent font size with main content
      color: #102048; // Same title color as in Details.vue
      line-height: 20px; // Adjust line height
    }

    .submodule-section {
      .submodule-item {
        margin-bottom: 6px;

        .submodule-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 4px;

          .module-info {
            font-weight: 500;
            color: #102048;
            font-size: 14px;
          }

          .module-score {
            font-size: 14px;
            color: #102048;
          }
        }

        .progress-bar-wrapper {
          margin-bottom: 4px;
          :deep(.ant-progress) {
            .ant-progress-outer {
              .ant-progress-inner {
                border-radius: 4px;
                overflow: hidden;
              }
            }
          }
        }

        .score-description {
          font-size: 12px;
          color: #102048;
          line-height: 1.4;
          padding-left: 0;
          margin: 4px 0;
        }

        &:last-child {
          margin-bottom: 2px;
        }
      }
    }
  }
}

.clickable-score {
  text-decoration: underline;
  cursor: pointer;
  color: #7CB305;
}
</style>
