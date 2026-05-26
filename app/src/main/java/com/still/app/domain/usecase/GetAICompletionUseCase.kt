package com.still.app.domain.usecase

import com.still.app.domain.repository.GeminiRepository
import javax.inject.Inject

class GetAiCompletionUseCase @Inject constructor(
    private val repository: GeminiRepository,
) {
    suspend operator fun invoke(context: String): Result<String?> =
        repository.getCompletion(context)
}

class GetAiCompletionVariantsUseCase @Inject constructor(
    private val repository: GeminiRepository,
) {
    suspend operator fun invoke(context: String, count: Int): Result<List<String>> =
        repository.getCompletionVariants(context, count)
}