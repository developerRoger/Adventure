/**
 * 基于vue.js的分页插件。
 * 
 * @author billy.lin
 * @version v1.0.0
 */
Vue.component("x-pagenation", {
	props: ["page", "queryObj", "pageQuery"],
	template: `
		<div class="text-center" v-if="this.page.total > 0">
			共[{{this.page.total}}]条数据，
			当前显示第[{{(pageNumber-1) * pageSize + 1}}] 至
			[{{(pageNumber * pageSize) > this.page.total? this.page.total:(pageNumber * pageSize)}}]条，
			每页显示<select v-model="pageSize" v-on:change="changePageSize">
				<option value="10">10</option>
				<option value="20">20</option>
				<option value="50">50</option>
			</select>条
			<a href="javascript:void(0)" v-on:click="backPage">上一页</a>
			<a href="javascript:void(0)" v-on:click="nextPage">下一页</a>
		</div>
		<div class="text-center" v-else-if="this.page.total == 0">
			暂无数据
		</div>
		<div class="text-center" v-else-if="this.page.total == null">
			<img src="/xframework/common/img/loading.gif" height="16px" />&nbsp;查询中，请稍后...
		</div>
	`,
	data: function() {
		return {
			pageSize: 10,
			pageNumber: 1
		}
	},
	methods: {
		changePageSize: function() {
			// 同时改变父VM的pageSize
			this.page.pageSize = this.pageSize;
			this.beginQuery();
		},
		nextPage: function() {
			if((this.pageNumber * this.pageSize) < this.page.total) {
				this.pageNumber = this.pageNumber + 1;
				// 同时改变父VM的pageNumber
				this.page.pageNumber = this.pageNumber;
				this.beginQuery();
			}
		},
		backPage: function() {
			if(this.pageNumber > 1) {
				this.pageNumber = this.pageNumber - 1;
				// 同时改变父VM的pageNumber
				this.page.pageNumber = this.pageNumber;
				this.beginQuery();
			}
		},
		beginQuery: function() {
			var newPage = {
				pageSize : this.pageSize,
				pageNumber : this.pageNumber,
				total : null
			}
			this.pageQuery(newPage);
		}
	}
});