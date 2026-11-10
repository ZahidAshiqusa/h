package com.zaidashiq.ultra.workers

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import com.zaidashiq.ultra.ZaidApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScheduledTaskWorker : JobService() {

    private val workerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartJob(params: JobParameters?): Boolean {
        val app = application as? ZaidApplication ?: return false
        workerScope.launch {
            app.container.engineManager.executeDueScheduledTasksNow()
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    companion object {
        fun enqueueSystemJob(context: Context, taskId: Long, delayMillis: Long) {
            try {
                val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
                val componentName = ComponentName(context, ScheduledTaskWorker::class.java)
                val jobId = (taskId.toInt() and 0x7FFFFFFF).coerceAtLeast(2000)
                val safeDelay = delayMillis.coerceAtLeast(1000L)
                val jobInfo = JobInfo.Builder(jobId, componentName)
                    .setMinimumLatency(safeDelay)
                    .setOverrideDeadline(safeDelay + 15_000L)
                    .setPersisted(false)
                    .build()
                scheduler.schedule(jobInfo)
            } catch (_: Throwable) {
            }
        }

        fun cancelSystemJob(context: Context, taskId: Long) {
            try {
                val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler ?: return
                val jobId = (taskId.toInt() and 0x7FFFFFFF).coerceAtLeast(2000)
                scheduler.cancel(jobId)
            } catch (_: Throwable) {
            }
        }
    }
}
