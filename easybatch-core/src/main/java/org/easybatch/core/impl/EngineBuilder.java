/*
 * The MIT License
 *
 *  Copyright (c) 2015, Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package org.easybatch.core.impl;

import org.easybatch.core.api.*;
import org.easybatch.core.api.event.EventManager;
import org.easybatch.core.api.event.job.JobEventListener;
import org.easybatch.core.api.event.step.*;
import org.easybatch.core.api.handler.ErrorRecordHandler;
import org.easybatch.core.api.handler.FilteredRecordHandler;
import org.easybatch.core.api.handler.IgnoredRecordHandler;
import org.easybatch.core.api.handler.RejectedRecordHandler;
import org.easybatch.core.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.easybatch.core.util.Utils.checkArgument;
import static org.easybatch.core.util.Utils.checkNotNull;

/**
 * Easy batch engine instance builder.
 *
 * @author Mahmoud Ben Hassine (mahmoud@benhassine.fr)
 */
public final class EngineBuilder {

    /**
     * The easy batch engine to build.
     */
    private EngineImpl engine;

    public EngineBuilder() {

        RecordReader recordReader = new NoOpRecordReader();
        List<RecordFilter> filterChain = new ArrayList<RecordFilter>();
        RecordSkipper recordSkipper = new RecordSkipper(-1);
        filterChain.add(new NoOpRecordFilter());
        RecordMapper recordMapper = new NoOpRecordMapper();
        List<RecordValidator> validationPipeline = new ArrayList<RecordValidator>();
        validationPipeline.add(new NoOpRecordValidator());
        List<RecordProcessor> processingPipeline = new ArrayList<RecordProcessor>();
        processingPipeline.add(new NoOpRecordProcessor());
        FilteredRecordHandler filteredRecordHandler = new NoOpFilteredRecordHandler();
        IgnoredRecordHandler ignoredRecordHandler = new NoOpIgnoredRecordHandler();
        RejectedRecordHandler rejectedRecordHandler = new NoOpRejectedRecordHandler();
        ErrorRecordHandler errorRecordHandler = new NoOpErrorRecordHandler();
        EventManager eventManager = new LocalEventManager();
        engine = new EngineImpl(
                Utils.DEFAULT_ENGINE_NAME,
                recordReader,
                recordSkipper,
                filterChain,
                recordMapper,
                validationPipeline,
                processingPipeline,
                filteredRecordHandler,
                ignoredRecordHandler,
                rejectedRecordHandler,
                errorRecordHandler,
                eventManager);
    }

    /**
     * Static method to create a new {@link EngineBuilder}.
     *
     * @return a new engine builder.
     */
    public static EngineBuilder aNewEngine() {
        return new EngineBuilder();
    }

    /**
     * Set the engine name.
     *
     * @param name the engine name
     * @return the engine builder
     */
    public EngineBuilder named(final String name) {
        checkNotNull(name, "engine name");
        engine.setName(name);
        return this;
    }

    /**
     * Set the number of records to skip by the engine.
     *
     * @param number the number of records to skip
     * @return the engine builder
     */
    public EngineBuilder skip(final long number) {
        checkArgument(number >= 1, "The number of records to skip should be greater than or equal to 1");
        engine.setRecordSkipper(new RecordSkipper(number));
        return this;
    }

    /**
     * Set the limit number of records to process by the engine.
     *
     * @param number the limit number of records to process
     * @return the engine builder
     */
    public EngineBuilder limit(final long number) {
        checkArgument(number >= 1, "The limit number of records should be greater than or equal to 1");
        engine.setLimit(number);
        return this;
    }

    /**
     * Register a record reader.
     *
     * @param recordReader the record reader to register
     * @return the engine builder
     */
    public EngineBuilder reader(final RecordReader recordReader) {
        checkNotNull(recordReader, "record reader");
        engine.setRecordReader(recordReader);
        return this;
    }

    /**
     * Register a record filter.
     *
     * @param recordFilter the record filter to register
     * @return the engine builder
     */
    public EngineBuilder filter(final RecordFilter recordFilter) {
        checkNotNull(recordFilter, "record filter");
        engine.addRecordFilter(recordFilter);
        return this;
    }

    /**
     * Register a record mapper.
     *
     * @param recordMapper the record mapper to register
     * @return the engine builder
     */
    public EngineBuilder mapper(final RecordMapper recordMapper) {
        checkNotNull(recordMapper, "record mapper");
        engine.setRecordMapper(recordMapper);
        return this;
    }

    /**
     * Register a record validator.
     *
     * @param recordValidator the record validator to register
     * @return the engine builder
     */
    public EngineBuilder validator(final RecordValidator recordValidator) {
        checkNotNull(recordValidator, "record validator");
        engine.addRecordValidator(recordValidator);
        return this;
    }

    /**
     * Register a record processor.
     *
     * @param recordProcessor the record processor to register
     * @return the engine builder
     */
    public EngineBuilder processor(final RecordProcessor recordProcessor) {
        checkNotNull(recordProcessor, "record processor");
        engine.addRecordProcessor(recordProcessor);
        return this;
    }

    /**
     * Register a record writer.
     *
     * @param recordWriter the record writer to register
     * @return the engine builder
     */
    public EngineBuilder writer(final RecordWriter recordWriter) {
        checkNotNull(recordWriter, "record writer");
        engine.addRecordProcessor(recordWriter);
        return this;
    }

    /**
     * Register a filtered record handler.
     *
     * @param filteredRecordHandler the handler to process filtered record
     * @return the engine builder
     */
    public EngineBuilder filteredRecordHandler(final FilteredRecordHandler filteredRecordHandler) {
        checkNotNull(filteredRecordHandler, "filtered record handler");
        engine.setFilteredRecordHandler(filteredRecordHandler);
        return this;
    }

    /**
     * Register a ignored record handler.
     *
     * @param ignoredRecordHandler the handler to process ignored record
     * @return the engine builder
     */
    public EngineBuilder ignoredRecordHandler(final IgnoredRecordHandler ignoredRecordHandler) {
        checkNotNull(ignoredRecordHandler, "ignored record handler");
        engine.setIgnoredRecordHandler(ignoredRecordHandler);
        return this;
    }

    /**
     * Register a rejected record handler.
     *
     * @param rejectedRecordHandler the handler to process rejected record
     * @return the engine builder
     */
    public EngineBuilder rejectedRecordHandler(final RejectedRecordHandler rejectedRecordHandler) {
        checkNotNull(rejectedRecordHandler, "rejected record handler");
        engine.setRejectedRecordHandler(rejectedRecordHandler);
        return this;
    }

    /**
     * Register a error record handler.
     *
     * @param errorRecordHandler the handler to process error record
     * @return the engine builder
     */
    public EngineBuilder errorRecordHandler(final ErrorRecordHandler errorRecordHandler) {
        checkNotNull(errorRecordHandler, "error record handler");
        engine.setErrorRecordHandler(errorRecordHandler);
        return this;
    }

    /**
     * Enable strict mode : if true, then the execution will be aborted on first mapping, validating or processing error.
     *
     * @param strictMode true if strict mode should be enabled
     * @return the engine builder
     */
    public EngineBuilder strictMode(final boolean strictMode) {
        engine.setStrictMode(strictMode);
        return this;
    }

    /**
     * Parameter to mute all loggers.
     *
     * @param silentMode true to enable silent mode
     * @return the engine builder
     */
    public EngineBuilder silentMode(final boolean silentMode) {
        engine.setSilentMode(silentMode);
        return this;
    }

    /**
     * Activate JMX monitoring.
     *
     * @param jmx true to enable jmx monitoring
     * @return the engine builder
     */
    public EngineBuilder enableJMX(final boolean jmx) {
        engine.enableJMX(jmx);
        return this;
    }

    /**
     * Register a job event listener.
     * See {@link JobEventListener} for available callback methods.
     *
     * @param jobEventListener The event listener to add.
     * @return the engine builder
     */
    public EngineBuilder jobEventListener(final JobEventListener jobEventListener) {
        checkNotNull(jobEventListener, "job event listener");
        engine.addJobEventListener(jobEventListener);
        return this;
    }

    /**
     * Register a record reader event listener.
     * See {@link org.easybatch.core.api.event.step.RecordReaderEventListener} for available callback methods.
     *
     * @param recordReaderEventListener The record reader listener to add.
     * @return the engine builder
     */
    public EngineBuilder recordReaderEventListener(final RecordReaderEventListener recordReaderEventListener) {
        checkNotNull(recordReaderEventListener, "record reader event listener");
        engine.addRecordReaderEventListener(recordReaderEventListener);
        return this;
    }

    /**
     * Register a record filter event listener.
     * See {@link org.easybatch.core.api.event.step.RecordFilterEventListener} for available callback methods.
     *
     * @param recordFilterEventListener The event listener to add.
     * @return the engine builder
     */
    public EngineBuilder recordFilterEventListener(final RecordFilterEventListener recordFilterEventListener) {
        checkNotNull(recordFilterEventListener, "record filter event listener");
        engine.addRecordFilterEventListener(recordFilterEventListener);
        return this;
    }

    /**
     * Register a record mapper event listener.
     * See {@link org.easybatch.core.api.event.step.RecordMapperEventListener} for available callback methods.
     *
     * @param recordMapperEventListener The event listener to add.
     * @return the engine builder
     */
    public EngineBuilder recordMapperEventListener(final RecordMapperEventListener recordMapperEventListener) {
        checkNotNull(recordMapperEventListener, "record mapper event listener");
        engine.addRecordMapperEventListener(recordMapperEventListener);
        return this;
    }

    /**
     * Register a record validator event listener.
     * See {@link org.easybatch.core.api.event.step.RecordValidatorEventListener} for available callback methods.
     *
     * @param recordValidatorEventListener The event listener to add.
     * @return the engine builder
     */
    public EngineBuilder recordValidatorEventListener(final RecordValidatorEventListener recordValidatorEventListener) {
        checkNotNull(recordValidatorEventListener, "record validator event listener");
        engine.addRecordValidatorEventListener(recordValidatorEventListener);
        return this;
    }

    /**
     * Register a record processor event listener.
     * See {@link org.easybatch.core.api.event.step.RecordProcessorEventListener} for available callback methods.
     *
     * @param recordProcessorEventListener The event listener to add.
     * @return the engine builder
     */
    public EngineBuilder recordProcessorEventListener(final RecordProcessorEventListener recordProcessorEventListener) {
        checkNotNull(recordProcessorEventListener, "record processor event listener");
        engine.addRecordProcessorEventListener(recordProcessorEventListener);
        return this;
    }

    /**
     * Register a custom event manager.
     *
     * @param eventManager The event manager to use instead of the default {@link org.easybatch.core.impl.LocalEventManager}
     * @return the engine builder
     */
    public EngineBuilder eventManager(final EventManager eventManager) {
        checkNotNull(eventManager, "event manager");
        engine.setEventManager(eventManager);
        return this;
    }

    /**
     * Build an Easy Batch engine instance.
     *
     * @return an Easy Batch instance
     */
    public Engine build() {
        return engine;
    }

}
