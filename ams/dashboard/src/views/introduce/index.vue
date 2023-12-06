<template>
  <div class="introduce-content">
    <section class="home-section">
      <div class="content">
        <div class="title">
          Amoro builds lake-native data warehouse and architecture for users, platforms and products
        </div>
        <img class="img" src="@/assets/images/home-content.png" />
      </div>
    </section>
    <div class="home-feature">
      <div class="content">
        <div class="title">Key Features</div>
        <div class="features">
          <div class="feature-item" v-for="item in features" :key="item.title">
            <div class="item-title">
              <img :src="item.icon" />
              {{ item.title }}
            </div>
            <div class="item-desc">{{ item.desc }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted } from 'vue'
import SelfManaged from '@/assets/images/Self-managed.png'
import MultipleFormats from '@/assets/images/Multiple-Formats.png'
import CatalogService from '@/assets/images/Catalog-Service.png'
import RichPlugins from '@/assets/images/Rich-Plugins.png'
import ManagementTools from '@/assets/images/Management-tools.png'
import InfrastructureIdependent from '@/assets/images/Infrastructure-Idependent.png'

export default defineComponent({
  name: 'Introduce',
  components: {},
  setup() {
    onMounted(() => {})
    const features = [
      {
        title: 'Self-optimizing',
        desc: 'Continuously optimizing tables, including compacting small files, change files, regularly delete expired files to keep high query performance and reducing storage costs.',
        icon: SelfManaged
      },
      {
        title: 'Multiple Formats',
        desc: 'Support different table formats such as Iceberg, Mixed-Iceberg and Mixed-Hive to meet different scenario requirements and provide them with unified management capabilities.',
        icon: MultipleFormats
      },
      {
        title: 'Catalog Service',
        desc: 'Provide an unified catalog service for all compute engines, which can also used with existing metadata store service such as Hive Metastore and AWS Glue.',
        icon: CatalogService
      },
      {
        title: 'Rich Plugins',
        desc: 'Provide various plugins to integrate with other systems, like continuously optimizing with Flink and data analysis with Spark and Kyuubi.',
        icon: RichPlugins
      },
      {
        title: 'Management Tools',
        desc: 'Provide a variety of management tools, including WEB UI and standard SQL command line, to help you get started faster and integrate with other systems more easily.',
        icon: ManagementTools
      },
      {
        title: 'Infrastructure Independent',
        desc: 'Can be easily deployed and used in private environments, cloud environments, hybrid cloud environments, and multi-cloud environments.',
        icon: InfrastructureIdependent
      }
    ]
    return {
      features
    }
  }
})
</script>

<style lang="less" scoped>
.home-section {
  margin: 0 auto;
  display: flex;
  justify-content: center;
  background-color: #fff;
  .content {
    width: 100%;
    max-width: 1182px;
    padding: 64px 30px;
    .img {
      max-width: 100%;
    }
  }
}

.home-section .content .title,
.home-feature .content .title {
  font-size: 20px;
  font-weight: bold;
  line-height: 24px;
  text-align: center;
  margin-bottom: 40px;
}
.home-feature {
  background: #f5f6fa;
  margin: 0 auto;
  display: flex;
  justify-content: center;
  .content {
    width: 100%;
    max-width: 1182px;
    padding: 64px 41px 80px;
    .title {
      font-size: 32px;
    }
    .features {
      display: grid;
      grid-row-gap: 24px;
      grid-column-gap: 24px;
      max-width: 1100px;
      grid-template-columns: auto auto;
      .feature-item {
        background-color: #fff;
        padding: 32px;
        border-radius: 5px;
        position: relative;
        .fix-icon {
          position: absolute;
          top: 0px;
          right: 20px;
        }
      }
    }
  }
}

@media screen and (max-width: 800px) {
  .home-feature .features {
    grid-template-columns: auto;
  }
}

.home-feature .features .item-title {
  font-size: 24px;
  font-weight: bold;
  line-height: 28px;
  display: flex;
  align-items: center;
}

.home-feature .features .item-title img {
  margin-right: 13px;
}

.home-feature .features .item-desc {
  margin-top: 24px;
  font-size: 14px;
  font-weight: normal;
  line-height: 22px;
  color: #53576a;
}
</style>
