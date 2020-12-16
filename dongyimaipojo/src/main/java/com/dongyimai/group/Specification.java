package com.dongyimai.group;

import com.dongyimai.pojo.TbSpecification;
import com.dongyimai.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

public class Specification implements Serializable {

    private TbSpecification tbSpecification;
    private List<TbSpecificationOption> tbSpecificationOptionList;

    public Specification() {
    }

    public TbSpecification getTbSpecification() {
        return tbSpecification;
    }

    public void setTbSpecification(TbSpecification tbSpecification) {
        this.tbSpecification = tbSpecification;
    }

    public List<TbSpecificationOption> getTbSpecificationOptionList() {
        return tbSpecificationOptionList;
    }

    public void setTbSpecificationOptionList(List<TbSpecificationOption> tbSpecificationOptionList) {
        this.tbSpecificationOptionList = tbSpecificationOptionList;
    }
}
