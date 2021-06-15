import React,{ useState } from 'react'
import axios from 'axios';
import ArrowForwardIosIcon from '@material-ui/icons/ArrowForwardIos';
function MediaLibrary(e){
    const data=useState( [] );
    if(this.state.setVisibleMenu)
    {
    this.setState(state=>({
        setVisibleMenu:false,
        menu:''
    }
    ));
}else if(!this.state.setVisibleMenu)
{
    this.setState(state=>({
        setVisibleMenu:true,
        menu:'menu'
}));
}
    axios({
      method: 'get',
      url: `http://mam-hackathon.com:8080/mediaHierarchy`
      })
      .then((response) => {
       response.data._embedded.mediaHierarchies.map((data, i) => {
          console.log('hierarchy name -'+data.hierarchyName);
         
        
        this.setState({
            data:response.data._embedded.mediaHierarchies
          })
       
        let childs=[]
           childs=data.children.map((child)=>{
            console.log('childre name '+child.hierarchyCode)
           
          });
       });
    });
  }
function Hierarchy() {
    return (
        <div>
             { this.state.data?.map((post, i) => (
      <div > <ArrowForwardIosIcon/> <div key={i} className="list-group-item">
       {post.hierarchyName}
      
       <div className={ this.state.submenu }  >
           <br/>
       {post.children?.map((po,i) =>
       <div key={i} className="list-group-item">
         {po.hierarchyName}
        
         </div>
       )}
       </div>
       <br/>
       </div>
       </div>
        ))
    
       }
      
        </div>
    )
}

export default Hierarchy
