package com.github.ttt374.healthcaretracer.ui.metric

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.metric.MetricCategory
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.metric.toEntry

@Composable
fun MetricScreen(viewModel: MetricViewModel = hiltViewModel()) {
    val measuredValuesMap = viewModel.metrics.mapValues { (_, stateFlow) ->
        stateFlow.collectAsState()
    }
    Scaffold { innerPadding ->
        LazyColumn(modifier=Modifier.padding(innerPadding)){
            items(MetricDefRegistry.getByCategory(MetricCategory.HEART)){ def ->
                Text(stringResource(def.resId))
                val entries = measuredValuesMap[def]?.value?.toEntry()
                entries?.let { entries.forEach { Text(it.toString())}}
            }
        }
    }
}