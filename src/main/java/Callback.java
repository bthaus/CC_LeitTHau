public interface Callback {
    //commentary applicable if used together with Task and Synchronizer
    //is called whenever the task is finished, even if it finished with an exception
    void onComplete();
    //is called when the given task throws an exception
    void onError(Exception e);
}
